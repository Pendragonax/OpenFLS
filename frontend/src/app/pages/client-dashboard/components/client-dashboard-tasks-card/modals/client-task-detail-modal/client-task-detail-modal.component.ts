import {Component} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {ClientTaskDto} from '../../../../../../shared/dtos/client-task-dto.model';
import {ClientTaskService} from '../../../../../../shared/services/client-task.service';
import {HelperService} from '../../../../../../shared/services/helper.service';
import {Converter} from '../../../../../../shared/services/converter.helper';

/**
 * View of one task plus its check-off actions. `task` is set on the component
 * instance right after opening (see ClientDashboardTasksCardComponent.openDetailModal),
 * following the same convention as the other lightweight modals in this app. The
 * description, and for a completed task the comment, are shown only here - the
 * dashboard card itself stays reduced to title, creator/date and due date so long
 * text never has to fit into the narrow card column.
 */
@Component({
  selector: 'app-client-task-detail-modal',
  templateUrl: './client-task-detail-modal.component.html',
  styleUrls: ['./client-task-detail-modal.component.css'],
  providers: [
    {provide: MAT_DATE_LOCALE, useValue: 'de-DE'},
    {provide: DateAdapter, useClass: NativeDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
  ],
  standalone: false
})
export class ClientTaskDetailModalComponent {

  task!: ClientTaskDto;
  isSubmitting = false;

  readonly completeForm: FormGroup<{
    comment: FormControl<string>;
    completedOn: FormControl<Date>;
  }>;

  constructor(
    public dialogRef: MatDialogRef<ClientTaskDetailModalComponent>,
    private formBuilder: NonNullableFormBuilder,
    private clientTaskService: ClientTaskService,
    private helperService: HelperService,
    private converter: Converter
  ) {
    this.completeForm = this.formBuilder.group({
      comment: this.formBuilder.control('', [Validators.maxLength(1024)]),
      completedOn: this.formBuilder.control(ClientTaskDetailModalComponent.today(), [Validators.required])
    });
  }

  /**
   * Closes with the updated task on success, so the card can refresh without a
   * separate "close" step - a snackbar already confirms what happened. Closing
   * without acting (via the button, backdrop or Escape) resolves to `null`, i.e.
   * "nothing changed".
   */
  complete() {
    if (this.isSubmitting || this.completeForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.clientTaskService.complete(this.task.id, {
      comment: this.completeForm.controls.comment.value,
      completedOn: this.converter.formatDate(this.completeForm.controls.completedOn.value)
    }).subscribe({
      next: (task) => {
        this.helperService.openSnackBar('Aufgabe abgehakt');
        this.dialogRef.close(task);
      },
      error: () => {
        this.isSubmitting = false;
        this.helperService.openSnackBar('Aufgabe konnte nicht abgehakt werden');
      }
    });
  }

  reopen() {
    if (this.isSubmitting) return;
    this.isSubmitting = true;
    this.clientTaskService.reopen(this.task.id, '').subscribe({
      next: task => { this.helperService.openSnackBar('Aufgabe wieder geöffnet'); this.dialogRef.close(task); },
      error: () => { this.isSubmitting = false; this.helperService.openSnackBar('Aufgabe konnte nicht wieder geöffnet werden'); }
    });
  }

  close() {
    this.dialogRef.close(null);
  }

  getDateString(value: string | null): string {
    return this.converter.getLocalDateString(value);
  }

  private static today(): Date {
    return new Date();
  }
}
