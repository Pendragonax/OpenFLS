import {Component} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {ClientTaskDto} from '../../../../../../shared/dtos/client-task-dto.model';
import {ClientTaskService} from '../../../../../../shared/services/client-task.service';
import {HelperService} from '../../../../../../shared/services/helper.service';
import {Converter} from '../../../../../../shared/services/converter.helper';

/**
 * Small dialog to create a task for a client. `clientId` is set on the component
 * instance right after opening (see ClientDashboardTasksCardComponent.openCreateModal),
 * following the same convention as the other lightweight modals in this app.
 */
@Component({
  selector: 'app-client-task-create-modal',
  templateUrl: './client-task-create-modal.component.html',
  styleUrls: ['./client-task-create-modal.component.css'],
  providers: [
    {provide: MAT_DATE_LOCALE, useValue: 'de-DE'},
    {provide: DateAdapter, useClass: NativeDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
  ],
  standalone: false
})
export class ClientTaskCreateModalComponent {

  clientId: number = 0;
  isSubmitting = false;

  readonly form: FormGroup<{
    title: FormControl<string>;
    description: FormControl<string>;
    dueDate: FormControl<Date>;
  }>;

  constructor(
    public dialogRef: MatDialogRef<ClientTaskCreateModalComponent>,
    private formBuilder: NonNullableFormBuilder,
    private clientTaskService: ClientTaskService,
    private helperService: HelperService,
    private converter: Converter
  ) {
    this.form = this.formBuilder.group({
      title: this.formBuilder.control('', [Validators.required, Validators.maxLength(128)]),
      description: this.formBuilder.control('', [Validators.maxLength(1024)]),
      dueDate: this.formBuilder.control(ClientTaskCreateModalComponent.today(), [Validators.required])
    });
  }

  submit() {
    if (this.isSubmitting || this.form.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.clientTaskService.create({
      clientId: this.clientId,
      title: this.form.controls.title.value,
      description: this.form.controls.description.value,
      dueDate: this.converter.formatDate(this.form.controls.dueDate.value)
    }).subscribe({
      next: (task: ClientTaskDto) => {
        this.helperService.openSnackBar('Aufgabe angelegt');
        this.dialogRef.close(task);
      },
      error: () => {
        this.isSubmitting = false;
        this.helperService.openSnackBar('Aufgabe konnte nicht angelegt werden');
      }
    });
  }

  cancel() {
    this.dialogRef.close(null);
  }

  private static today(): Date {
    return new Date();
  }
}
