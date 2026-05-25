import {Component, OnInit} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {ReplaySubject} from 'rxjs';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ConfirmationModalComponent} from '../../../../shared/modals/confirmation-modal/confirmation-modal.component';
import {EvaluationDto} from '../../../../shared/dtos/evaluation-dto.model';
import {EvaluationsService} from '../../../../shared/services/evaluations.service';
import {DateService} from '../../../../shared/services/date.service';
import {EvaluationRequestDto} from '../../../../shared/dtos/evaluation-request-dto.model';
import {HelperService} from '../../../../shared/services/helper.service';

@Component({
  selector: 'app-assistance-plan-analysis-modal',
  templateUrl: './assistance-plan-evaluation-modal.component.html',
  styleUrls: ['./assistance-plan-evaluation-modal.component.css'],
  standalone: false
})
export class AssistancePlanEvaluationModalComponent implements OnInit {
  evaluation$: ReplaySubject<EvaluationDto> = new ReplaySubject();
  evaluation: EvaluationDto = new EvaluationDto();
  goalId$: ReplaySubject<number> = new ReplaySubject<number>();
  goalId = 0;
  date$: ReplaySubject<Date> = new ReplaySubject<Date>();
  date: Date = new Date(Date.now());
  readOnly$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
  readOnly = false;

  newEvaluation = true;
  description = '';
  isExecuting$: ReplaySubject<boolean> = new ReplaySubject();
  isExecuting = false;
  operationErrorMessage = '';

  readonly TITLE_NEW: string = 'neue Evaluation erstellen';
  readonly TITLE_EXISTING: string = 'Evaluation bearbeiten';
  readonly TITLE_READ_ONLY: string = 'Evaluation anzeigen';
  readonly SAVE_BUTTON_DESCRIPTION: string = 'Speichern';
  readonly DELETE_BUTTON_DESCRIPTION: string = 'Löschen';
  readonly CLOSE_BUTTON_DESCRIPTION: string = 'Schließen';

  inputGroup: UntypedFormGroup = new UntypedFormGroup({
    description: new UntypedFormControl({value: this.description, disabled: false}),
    timesAdhered: new UntypedFormControl({value: true, disabled: false}),
  });

  get descriptionControl() { return this.inputGroup.controls['description']; }
  get timesAdheredControl() { return this.inputGroup.controls['timesAdhered']; }

  constructor(
    private evaluationService: EvaluationsService,
    private dialogRef: MatDialogRef<AssistancePlanEvaluationModalComponent>,
    private matDialog: MatDialog,
    private dateService: DateService,
    private helperService: HelperService
  ) { }

  ngOnInit(): void {
    this.initializeFormControls();
    this.evaluation$.subscribe({
      next: value => {
        if (value != null) {
          this.evaluation = value;
          this.newEvaluation = value.id <= 0;
          this.descriptionControl.setValue(this.evaluation.content);
          this.timesAdheredControl.setValue(this.evaluation.approved);
          this.description = this.evaluation.content;
        } else {
          this.evaluation = new EvaluationDto();
          this.evaluation.approved = true;
          this.description = '';
        }

        this.applyControlState();
      }
    });
    this.goalId$.subscribe({
      next: value => {
        this.goalId = value;
      }
    });
    this.date$.subscribe({
      next: value => {
        this.date = value;
      }
    });
    this.readOnly$.subscribe({
      next: value => {
        this.readOnly = value;
        this.applyControlState();
      }
    });
    this.isExecuting$.subscribe({
      next: value => {
        this.isExecuting = value;
        this.applyControlState();
      }
    });
  }

  initializeFormControls() {
    this.descriptionControl.valueChanges.subscribe({
      next: value => {
        this.evaluation.content = value;
        this.description = value;
      }
    });
    this.timesAdheredControl.valueChanges.subscribe({
      next: value => this.evaluation.approved = value
    });
  }

  private applyControlState() {
    if (this.readOnly || this.isExecuting) {
      this.descriptionControl.disable({emitEvent: false});
      this.timesAdheredControl.disable({emitEvent: false});
      return;
    }

    this.descriptionControl.enable({emitEvent: false});
    this.timesAdheredControl.enable({emitEvent: false});
  }

  convertDateStringToString(date: Date): String {
    return this.dateService.formatDateString(date.toLocaleString());
  }

  onSaveClick() {
    if (this.readOnly) {
      return;
    }

    this.operationErrorMessage = '';
    this.isExecuting$.next(true);

    const evaluationRequest: EvaluationRequestDto = new EvaluationRequestDto();
    this.evaluation.goalId = this.goalId;
    evaluationRequest.goalId = this.evaluation.goalId;
    evaluationRequest.id = this.evaluation.id;
    evaluationRequest.content = this.evaluation.content;
    evaluationRequest.approved = this.evaluation.approved;

    if (this.newEvaluation) {
      evaluationRequest.id = 0;
      evaluationRequest.date = this.dateService.formatDateToYearMonthDay(this.date);
      this.evaluationService.create(evaluationRequest).subscribe({
        next: _ => {
          this.isExecuting$.next(false);
          this.closeDialog();
        },
        error: error => this.handleFailure(error, 'Evaluation konnte nicht erstellt werden')
      });
    } else {
      evaluationRequest.date = this.evaluation.date;
      this.evaluationService.update(evaluationRequest).subscribe({
        next: _ => {
          this.isExecuting$.next(false);
          this.closeDialog();
        },
        error: error => this.handleFailure(error, 'Evaluation konnte nicht gespeichert werden')
      });
    }
  }

  onDeleteClick() {
    if (this.readOnly) {
      return;
    }

    const dialogRef = this.matDialog.open(ConfirmationModalComponent);
    const dialog = dialogRef.componentInstance;
    dialog.description = 'Wollen sie diese Evaluation wirklich löschen?';
    dialogRef.afterClosed().subscribe({
      next: value => {
        if (value) {
          this.evaluationService.delete(this.evaluation.id).subscribe({
            next: _ => {
              this.isExecuting$.next(false);
              this.closeDialog();
            },
            error: error => this.handleFailure(error, 'Evaluation konnte nicht gelöscht werden')
          });
        }
      }
    });
  }

  private handleFailure(error: unknown, fallbackMessage: string) {
    const message = this.extractErrorMessage(error) ?? fallbackMessage;
    this.operationErrorMessage = message;
    this.helperService.openSnackBar(message);
    this.isExecuting$.next(false);
  }

  private extractErrorMessage(error: unknown): string | null {
    if (error instanceof HttpErrorResponse) {
      const responseBody = error.error;
      return this.getMessageFromBody(responseBody) ?? error.message ?? null;
    }

    const maybeError = error as { error?: unknown; message?: string } | null;
    if (maybeError?.error != null) {
      const nestedMessage = this.getMessageFromBody(maybeError.error);
      if (nestedMessage != null) {
        return nestedMessage;
      }
    }

    return maybeError?.message ?? null;
  }

  private getMessageFromBody(body: unknown): string | null {
    if (body == null) {
      return null;
    }

    if (typeof body === 'string') {
      return body;
    }

    const structuredBody = body as { message?: string; detail?: string; title?: string; error?: string };
    return structuredBody.message ?? structuredBody.detail ?? structuredBody.title ?? structuredBody.error ?? null;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
