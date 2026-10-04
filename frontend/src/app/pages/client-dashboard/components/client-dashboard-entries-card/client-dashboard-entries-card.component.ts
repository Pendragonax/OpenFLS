import {Component, Input} from '@angular/core';
import {ClientDashboardAccess, ClientLatestServiceDto} from '../../../../shared/dtos/client-dashboard-dto.model';
import {Converter} from '../../../../shared/services/converter.helper';

/** Number of entries the dashboard requests from the backend, kept in sync with
 *  ClientDashboardService.LATEST_SERVICE_COUNT on the backend. */
export const LATEST_ENTRIES_LIMIT = 5;

/**
 * Short view of the last entries of a client: date, title, content and who
 * documented it. Each row links to the entries of that day; "Alle Einträge" opens
 * the full list preselected for the client and, when known, the period of the
 * relevant assistance plan.
 */
@Component({
  selector: 'app-client-dashboard-entries-card',
  templateUrl: './client-dashboard-entries-card.component.html',
  styleUrls: ['./client-dashboard-entries-card.component.css'],
  standalone: false
})
export class ClientDashboardEntriesCardComponent {

  @Input() access: ClientDashboardAccess = 'GRANTED';
  @Input() entries: ClientLatestServiceDto[] = [];
  @Input() clientId: number = 0;
  @Input() currentEmployeeId: number = 0;
  @Input() allEntriesLink: unknown[] = ['/services/all'];

  readonly limit = LATEST_ENTRIES_LIMIT;

  constructor(private converter: Converter) {
  }

  get isDenied(): boolean {
    return this.access === 'DENIED';
  }

  getDateString(value: string): string {
    return this.converter.getLocalDateString(this.toDateOnly(value));
  }

  getTimeString(value: string): string {
    const date = new Date(value);
    if (isNaN(date.getTime())) {
      return '';
    }

    return date.toLocaleTimeString('de-DE', {hour: '2-digit', minute: '2-digit'});
  }

  getEmployeeName(entry: ClientLatestServiceDto): string {
    return `${entry.employeeFirstname} ${entry.employeeLastname}`.trim();
  }

  /** Subtle cue for entries documented by the person currently looking at the dashboard. */
  isOwnEntry(entry: ClientLatestServiceDto): boolean {
    return this.currentEmployeeId > 0 && entry.employeeId === this.currentEmployeeId;
  }

  /** Route into the entry list of the day the entry belongs to. */
  getDayRouterLink(entry: ClientLatestServiceDto): unknown[] {
    const day = this.toDateOnly(entry.start);
    return ['/services/all', day, day, 0, 0, this.clientId];
  }

  private toDateOnly(value: string): string {
    return (value ?? '').substring(0, 10);
  }
}
