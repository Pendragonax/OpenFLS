import {Component} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {MatDialogRef} from '@angular/material/dialog';
import {ClientTaskDto} from '../../../../../../shared/dtos/client-task-dto.model';
import {ClientTaskService} from '../../../../../../shared/services/client-task.service';
import {Converter} from '../../../../../../shared/services/converter.helper';
import {HelperService} from '../../../../../../shared/services/helper.service';

@Component({
  selector: 'app-client-task-edit-modal',
  templateUrl: './client-task-edit-modal.component.html',
  styleUrls: ['./client-task-edit-modal.component.css'],
  providers: [
    {provide: MAT_DATE_LOCALE, useValue: 'de-DE'},
    {provide: DateAdapter, useClass: NativeDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
  ],
  standalone: false
})
export class ClientTaskEditModalComponent {
  task!: ClientTaskDto;
  isSubmitting = false;
  readonly form: FormGroup<{title: FormControl<string>; description: FormControl<string>; dueDate: FormControl<Date>}>;

  constructor(public dialogRef: MatDialogRef<ClientTaskEditModalComponent>, private fb: NonNullableFormBuilder,
              private taskService: ClientTaskService, private converter: Converter, private helper: HelperService) {
    this.form = this.fb.group({
      title: this.fb.control('', [Validators.required, Validators.maxLength(128)]),
      description: this.fb.control('', [Validators.maxLength(1024)]),
      dueDate: this.fb.control(new Date(), [Validators.required])
    });
  }

  initialize(task: ClientTaskDto) {
    this.task = task;
    const [year, month, day] = task.dueDate.split('-').map(Number);
    this.form.setValue({title: task.title, description: task.description, dueDate: new Date(year, month - 1, day)});
  }

  submit() {
    if (this.form.invalid || this.isSubmitting) return;
    this.isSubmitting = true;
    this.taskService.update(this.task.id, {
      title: this.form.controls.title.value,
      description: this.form.controls.description.value,
      dueDate: this.converter.formatDate(this.form.controls.dueDate.value)
    }).subscribe({next: task => { this.helper.openSnackBar('Aufgabe geändert'); this.dialogRef.close(task); },
      error: () => { this.isSubmitting = false; this.helper.openSnackBar('Aufgabe konnte nicht geändert werden'); }});
  }

  cancel() { this.dialogRef.close(null); }
}
