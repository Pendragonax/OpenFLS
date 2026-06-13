import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ClientArchiveExportForm} from "../../forms/client-archive-export-form";
import {ClientArchiveExportFormat} from "../../../../shared/dtos/client-archive-export-format.model";
import {ClientArchiveExportStatusDto} from "../../../../shared/dtos/client-archive-export-status-dto.model";

@Component({
  selector: 'app-client-archive-export-panel',
  templateUrl: './client-archive-export-panel.component.html',
  styleUrls: ['./client-archive-export-panel.component.css'],
  host: {class: 'archive-panel archive-panel--export'},
  standalone: false
})
export class ClientArchiveExportPanelComponent {
  private static readonly archiveExportStepOrder = ['requested', 'generating', 'ready'] as const;

  @Input({required: true}) archiveExportForm!: ClientArchiveExportForm;
  @Input() archiveExportStatus: ClientArchiveExportStatusDto | null = null;
  @Input({required: true}) isRequesting = false;
  @Input({required: true}) isDownloading = false;

  @Output() requestExport = new EventEmitter<void>();
  @Output() downloadExport = new EventEmitter<void>();

  readonly archiveExportFormats = [
    {value: ClientArchiveExportFormat.JSON, label: 'JSON'}
  ];
  readonly archiveExportSteps = [
    {key: 'requested', title: 'Angefordert'},
    {key: 'generating', title: 'Wird erstellt'},
    {key: 'ready', title: 'Bereit zum Download'}
  ] as const;

  get hasDownloadLink(): boolean {
    return this.archiveExportStatus?.downloadLink != null;
  }

  get archiveExportStage(): 'requested' | 'generating' | 'ready' {
    if (this.isRequesting) {
      return 'generating';
    }

    if (this.hasDownloadLink) {
      return 'ready';
    }

    return 'requested';
  }

  get archiveExportDownloadExpiresAt(): string {
    const expiresAt = this.archiveExportStatus?.downloadLink?.downloadLinkExpiresAt ?? null;
    if (!expiresAt) {
      return '';
    }

    return new Date(expiresAt).toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }) + ' ' + new Date(expiresAt).toLocaleTimeString('de-DE', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isArchiveExportStepCompleted(step: 'requested' | 'generating' | 'ready'): boolean {
    return ClientArchiveExportPanelComponent.archiveExportStepOrder.indexOf(step) <
      ClientArchiveExportPanelComponent.archiveExportStepOrder.indexOf(this.archiveExportStage);
  }

  isArchiveExportStepCurrent(step: 'requested' | 'generating' | 'ready'): boolean {
    return this.archiveExportStage === step;
  }
}
