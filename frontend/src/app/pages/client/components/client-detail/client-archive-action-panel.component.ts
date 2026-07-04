import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ClientArchiveActionForm} from "../../forms/client-archive-action-form";

@Component({
  selector: 'app-client-archive-action-panel',
  templateUrl: './client-archive-action-panel.component.html',
  styleUrls: ['./client-archive-action-panel.component.css'],
  host: {class: 'archive-panel archive-panel--form'},
  standalone: false
})
export class ClientArchiveActionPanelComponent {
  @Input({required: true}) archiveActionForm!: ClientArchiveActionForm;
  @Input({required: true}) archived = false;
  @Input({required: true}) isSubmitting = false;

  @Output() confirm = new EventEmitter<void>();
}
