import {Component, Input} from '@angular/core';
import {AssistancePlanPreviewDto} from '../../../../shared/dtos/assistance-plan-preview-dto.model';
import {ClientDashboardAccess} from '../../../../shared/dtos/client-dashboard-dto.model';
import {
  AssistancePlanProgress,
  AssistancePlanStatusPeriod
} from '../../../../shared/helpers/assistance-plan-progress.helper';
import {Converter} from '../../../../shared/services/converter.helper';

/**
 * Current assistance plan of a client together with its evaluation. Corridor plans
 * and exact plans are read differently, therefore both the displayed hours and the
 * meaning of the bar depend on the hour mode.
 */
@Component({
  selector: 'app-client-dashboard-plan-card',
  templateUrl: './client-dashboard-plan-card.component.html',
  styleUrls: ['./client-dashboard-plan-card.component.css'],
  standalone: false
})
export class ClientDashboardPlanCardComponent {

  @Input() access: ClientDashboardAccess = 'GRANTED';
  @Input() preview: AssistancePlanPreviewDto | null = null;
  @Input() assistancePlanCount: number = 0;
  @Input() clientId: number = 0;

  statusPeriod: AssistancePlanStatusPeriod = 'assistancePlan';

  constructor(private converter: Converter) {
  }

  get isDenied(): boolean {
    return this.access === 'DENIED';
  }

  get isCorridorPlan(): boolean {
    return this.preview != null && AssistancePlanProgress.isCorridorPlan(this.preview);
  }

  /**
   * The selected plan is the running one when available, otherwise the plan whose
   * end lies closest to today (which may still be in the future). The title makes
   * that distinction visible instead of always claiming "current".
   */
  get planTitle(): string {
    if (this.preview == null) {
      return 'Hilfeplan';
    }
    if (this.preview.isActive) {
      return 'Aktueller Hilfeplan';
    }

    return this.preview.start > this.todayIsoDate() ? 'Nächster Hilfeplan' : 'Aktuellster Hilfeplan';
  }

  get hourModeLabel(): string {
    return this.isCorridorPlan ? 'Stundenkorridor' : 'Feste Wochenstunden';
  }

  get weeklyHours(): string {
    return this.preview == null ? '' : AssistancePlanProgress.getWeeklyHoursDisplay(this.preview);
  }

  get executedHours(): number {
    return this.preview == null
      ? 0
      : AssistancePlanProgress.getExecutedHours(this.preview, this.statusPeriod);
  }

  get approvedHours(): number {
    return this.preview == null
      ? 0
      : AssistancePlanProgress.getApprovedHours(this.preview, this.statusPeriod);
  }

  get approvedHoursFrom(): number {
    return this.preview == null
      ? 0
      : AssistancePlanProgress.getApprovedHoursFrom(this.preview, this.statusPeriod);
  }

  get approvedHoursTill(): number {
    return this.preview == null
      ? 0
      : AssistancePlanProgress.getApprovedHoursTill(this.preview, this.statusPeriod);
  }

  get progressPercent(): number {
    return this.preview == null
      ? 0
      : AssistancePlanProgress.getExecutedHoursPercent(this.preview, this.statusPeriod);
  }

  get progressClass(): string {
    return this.preview == null
      ? ''
      : AssistancePlanProgress.getExecutedHoursProgressClass(this.preview, this.statusPeriod);
  }

  get hoursLeftDisplay(): string {
    return this.preview == null
      ? ''
      : AssistancePlanProgress.getApprovedHoursLeftDisplay(this.preview, this.statusPeriod);
  }

  /** Corridor plans are on track inside their corridor, exact plans close to 100 percent. */
  get progressSummary(): string {
    if (this.preview == null) {
      return '';
    }

    if (this.isCorridorPlan) {
      const percent = this.progressPercent;
      if (percent < 40) {
        return 'Unterhalb des Korridors';
      }
      if (percent > 60) {
        return 'Oberhalb des Korridors';
      }
      return 'Innerhalb des Korridors';
    }

    return `${AssistancePlanProgress.formatHourValue(this.progressPercent)} % der bewilligten Stunden`;
  }

  setStatusPeriod(period: AssistancePlanStatusPeriod) {
    this.statusPeriod = period;
  }

  getDateString(date: string | null): string {
    return this.converter.getLocalDateString(date);
  }

  private todayIsoDate(): string {
    return new Date().toISOString().substring(0, 10);
  }
}
