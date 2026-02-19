import {Component, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {EvaluationDto} from "../../../../shared/dtos/evaluation-dto.model";
import {EvaluationsService} from "../../../../shared/services/evaluations.service";
import {DateService} from "../../../../shared/services/date.service";
import {EvaluationRequestDto} from "../../../../shared/dtos/evaluation-request-dto.model";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ConfirmationModalComponent} from "../../../../shared/modals/confirmation-modal/confirmation-modal.component";

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
  goalId: number = 0;
  date$: ReplaySubject<Date> = new ReplaySubject<Date>();
  date: Date = new Date(Date.now());

  newEvaluation: boolean = true;
  description: string = '';
  isExecuting$: ReplaySubject<boolean> = new ReplaySubject();
  isExecuting: boolean = false;

  readonly TITLE_NEW: string = "neue Evaluation erstellen"
  readonly TITLE_EXISTING: string = "Evaluation bearbeiten"
  readonly SAVE_BUTTON_DESCRIPTION: string = "Speichern"
  readonly DELETE_BUTTON_DESCRIPTION: string = "Löschen"
  readonly CLOSE_BUTTON_DESCRIPTION: string = "Schließen"

  inputGroup: UntypedFormGroup = new UntypedFormGroup({
    description: new UntypedFormControl({value: this.description, disabled: false}),
    timesAdhered: new UntypedFormControl({value: true, disabled: false}),
  })
  // GETTER
  get descriptionControl() { return this.inputGroup.controls['description']; }
  get timesAdheredControl() { return this.inputGroup.controls['timesAdhered']; }

  constructor(
    private evaluationService: EvaluationsService,
    private dialogRef: MatDialogRef<AssistancePlanEvaluationModalComponent>,
    private matDialog: MatDialog,
    private dateService: DateService
  ) { }

  ngOnInit(): void {
    this.initializeFormControls();
    this.evaluation$.subscribe({
      next: value => {
        if (value != null) {
          this.evaluation = value;
          this.newEvaluation = value.id <= 0;
          this.descriptionControl.setValue(this.evaluation.content)
          this.timesAdheredControl.setValue(this.evaluation.approved)
        } else {
          this.evaluation = new EvaluationDto()
          this.evaluation.approved = true
        }
      }
    })
    this.goalId$.subscribe({
      next: value => {
        this.goalId = value
      }
    })
    this.date$.subscribe({
      next: value => {
        this.date = value
      }
    })
    this.isExecuting$.subscribe({
      next: value => {
        this.isExecuting = value

        if (value) {
          this.descriptionControl.disable()
          this.timesAdheredControl.disable()
        } else {
          this.descriptionControl.enable()
          this.timesAdheredControl.enable()
        }
      }
    })
  }

  initializeFormControls() {
    this.descriptionControl.valueChanges.subscribe({
      next: value => {
        this.evaluation.content = value
        this.description = value
      }
    })
    this.timesAdheredControl.valueChanges.subscribe({
      next: value => this.evaluation.approved = value
    })
  }

  convertDateStringToString(date: Date): String {
    return this.dateService.formatDateString(date.toLocaleString())
  }

  onSaveClick() {
    this.isExecuting$.next(true)

    let evaluationRequest: EvaluationRequestDto = new EvaluationRequestDto()
    this.evaluation.goalId = this.goalId
    evaluationRequest.goalId = this.evaluation.goalId
    evaluationRequest.id = this.evaluation.id
    evaluationRequest.content = this.evaluation.content
    evaluationRequest.approved = this.evaluation.approved

    if (this.newEvaluation) {
      evaluationRequest.id = 0
      evaluationRequest.date = this.dateService.formatDateToYearMonthDay(this.date)
      this.evaluationService.create(evaluationRequest).subscribe({
        next: _ => {
          this.isExecuting$.next(false)
          this.closeDialog()
        },
        error: _ => this.isExecuting$.next(false)
      })
    } else {
      evaluationRequest.date = this.evaluation.date
      this.evaluationService.update(evaluationRequest).subscribe({
        next: _ => {
          this.isExecuting$.next(false)
          this.closeDialog()
        },
        error: _ => this.isExecuting$.next(false)
      })
    }
  }

  onDeleteClick() {
    let dialogRef = this.matDialog.open(ConfirmationModalComponent);
    let dialog = dialogRef.componentInstance;
    dialog.description = "Wollen sie diese Evaluation wirklich löschen?";
    dialogRef.afterClosed().subscribe({
      next: value => {
        if (value) {
          this.evaluationService.delete(this.evaluation.id).subscribe({
            next: _ => {
              this.isExecuting$.next(false)
              this.closeDialog()
            },
            error: _ => this.isExecuting$.next(false)
          })
        }
      }
    })
  }

  closeDialog() {
    this.dialogRef.close()
  }
}
