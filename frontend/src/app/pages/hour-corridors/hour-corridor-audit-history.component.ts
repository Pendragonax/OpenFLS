import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {HourCorridorAuditAction, HourCorridorAuditLogDto} from '../../shared/dtos/hour-corridor-audit-log.dto';
import {HourCorridorService} from '../../shared/services/hour-corridor.service';

@Component({
  selector: 'app-hour-corridor-audit-history',
  templateUrl: './hour-corridor-audit-history.component.html',
  styleUrls: ['./hour-corridor-audit-history.component.css'],
  standalone: false
})
export class HourCorridorAuditHistoryComponent implements OnInit {
  @Input() corridorTitle = '';
  @Input() corridorId = 0;
  entries: HourCorridorAuditLogDto[] = [];
  isLoading = true;
  hasError = false;

  constructor(
    private service: HourCorridorService,
    public activeModal: NgbActiveModal
  ) {}

  ngOnInit(): void {
    this.service.getAuditHistory(this.corridorId).subscribe({
      next: entries => {
        this.entries = [...entries].sort((a, b) =>
          new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime() || b.id - a.id);
        this.isLoading = false;
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  actionLabel(action: HourCorridorAuditAction): string {
    return {CREATE: 'Erstellt', UPDATE: 'Geändert', DELETE: 'Gelöscht'}[action];
  }

  formatMinutes(value: number | null): string {
    if (value == null) return '–';
    return `${Math.floor(value / 60)}:${String(value % 60).padStart(2, '0')} h`;
  }
}
