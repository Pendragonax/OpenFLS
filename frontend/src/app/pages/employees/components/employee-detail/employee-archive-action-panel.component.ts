import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EmployeeArchiveActionForm} from "../../forms/employee-archive-action-form";

@Component({
  selector: 'app-employee-archive-action-panel',
  templateUrl: './employee-archive-action-panel.component.html',
  styleUrls: ['./employee-archive-action-panel.component.css'],
  host: {class: 'archive-panel archive-panel--form'},
  standalone: false
})
export class EmployeeArchiveActionPanelComponent {
  @Input({required: true}) archiveActionForm!: EmployeeArchiveActionForm;
  @Input({required: true}) archived = false;
  @Input({required: true}) isSubmitting = false;

  @Output() confirm = new EventEmitter<void>();
}
