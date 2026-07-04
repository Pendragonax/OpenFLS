import {Component, Input} from '@angular/core';
import {EmployeeArchiveHistoryEntryReadDto} from "../../../../shared/dtos/employee-archive-history-entry-read-dto.model";
import {Converter} from "../../../../shared/services/converter.helper";

@Component({
  selector: 'app-employee-archive-history-panel',
  templateUrl: './employee-archive-history-panel.component.html',
  styleUrls: ['./employee-archive-history-panel.component.css'],
  host: {class: 'archive-panel archive-panel--history'},
  standalone: false
})
export class EmployeeArchiveHistoryPanelComponent {
  @Input({required: true}) archiveHistory: EmployeeArchiveHistoryEntryReadDto[] = [];

  constructor(private converter: Converter) {
  }

  formatArchiveActionType(actionType: string): string {
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

  formatArchiveEmployee(entry: EmployeeArchiveHistoryEntryReadDto): string {
    return `${entry.executingEmployeeFirstname} ${entry.executingEmployeeLastname} (#${entry.executingEmployeeId})`;
  }
}
