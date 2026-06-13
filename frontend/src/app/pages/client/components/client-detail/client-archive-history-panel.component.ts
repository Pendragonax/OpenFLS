import {Component, Input} from '@angular/core';
import {ClientArchiveHistoryEntryReadDto} from "../../../../shared/dtos/client-archive-history-entry-read-dto.model";
import {Converter} from "../../../../shared/services/converter.helper";

@Component({
  selector: 'app-client-archive-history-panel',
  templateUrl: './client-archive-history-panel.component.html',
  styleUrls: ['./client-archive-history-panel.component.css'],
  host: {class: 'archive-panel archive-panel--history'},
  standalone: false
})
export class ClientArchiveHistoryPanelComponent {
  @Input({required: true}) archiveHistory: ClientArchiveHistoryEntryReadDto[] = [];

  constructor(private converter: Converter) {
  }

  formatArchiveActionType(actionType: string): string {
    if (actionType === 'EXPORT') {
      return 'EXPORT';
    }

    if (actionType === 'REACTIVATE') {
      return 'Reaktivierung';
    }

    return 'Archivierung';
  }

  formatArchiveActionDate(actionDate: string | Date | null): string {
    if (!actionDate) {
      return '';
    }

    const date = actionDate instanceof Date ? actionDate : new Date(actionDate);
    return this.converter.formatDateToGerman(date);
  }

  formatArchiveActionTimestamp(actionTimestamp: string | Date | null): string {
    if (!actionTimestamp) {
      return '';
    }

    const timestamp = actionTimestamp instanceof Date ? actionTimestamp : new Date(actionTimestamp);
    return this.converter.formatDateToGermanTime(timestamp);
  }

  formatArchiveEmployee(entry: ClientArchiveHistoryEntryReadDto): string {
    return `${entry.executingEmployeeFirstname} ${entry.executingEmployeeLastname} (#${entry.executingEmployeeId})`;
  }

  shouldShowArchiveHistoryDate(actionType: string): boolean {
    return actionType !== 'EXPORT';
  }

  shouldShowArchiveHistoryReason(actionType: string): boolean {
    return actionType !== 'EXPORT';
  }
}
