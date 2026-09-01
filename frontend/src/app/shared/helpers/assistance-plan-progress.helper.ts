import {AssistancePlanPreviewDto} from "../dtos/assistance-plan-preview-dto.model";
import {AssistancePlanHourMode} from "../dtos/assistance-plan-hour-mode.model";

export type AssistancePlanStatusPeriod = 'assistancePlan' | 'year';

/**
 * Shared reading of the approved/executed hours of an assistance plan. The
 * favourite table and the client dashboard show the same numbers, therefore the
 * calculation lives in one place. Corridor plans are read differently from exact
 * plans: they are "on track" inside their corridor, not at 100 percent.
 */
export class AssistancePlanProgress {

  static isCorridorPlan(preview: AssistancePlanPreviewDto): boolean {
    return preview.hourMode === AssistancePlanHourMode.CORRIDOR;
  }

  static getExecutedHoursPercent(
    preview: AssistancePlanPreviewDto,
    period: AssistancePlanStatusPeriod = 'year'
  ): number {
    if (this.isCorridorPlan(preview)) {
      return this.getCorridorExecutedHoursPercent(preview, period);
    }

    const approvedMinutes = this.convertTimeDoubleToMinutes(this.getApprovedHours(preview, period));
    if (approvedMinutes <= 0) {
      return 0;
    }

    const executedMinutes = this.convertTimeDoubleToMinutes(this.getExecutedHours(preview, period));
    const percent = (executedMinutes * 100) / approvedMinutes;
    return Math.max(0, Math.min(100, Number(percent.toFixed(1))));
  }

  static getExecutedHoursProgressClass(
    preview: AssistancePlanPreviewDto,
    period: AssistancePlanStatusPeriod = 'year'
  ): string {
    const percent = this.getExecutedHoursPercent(preview, period);
    if (this.isCorridorPlan(preview)) {
      if (percent >= 40 && percent <= 60) {
        return 'hours-progress-fill--ok';
      }
      return 'hours-progress-fill--bad';
    }

    if (percent >= 95) {
      return 'hours-progress-fill--ok';
    }
    if (percent >= 90) {
      return 'hours-progress-fill--warn';
    }
    return 'hours-progress-fill--bad';
  }

  static getHourModeRange(preview: AssistancePlanPreviewDto): string {
    if (!this.isCorridorPlan(preview)) {
      return '';
    }

    return `${this.formatHourValue(preview.approvedHoursFrom)} - ${this.formatHourValue(preview.approvedHoursTo)}`;
  }

  static getWeeklyHoursDisplay(preview: AssistancePlanPreviewDto): string {
    if (this.isCorridorPlan(preview)) {
      return this.getHourModeRange(preview);
    }

    return `${this.formatHourValue(preview.approvedHoursPerWeek)}`;
  }

  static getApprovedHoursLeftDisplay(
    preview: AssistancePlanPreviewDto,
    period: AssistancePlanStatusPeriod = 'year'
  ): string {
    const value = this.getApprovedHoursLeft(preview, period);
    const sign = value < 0 ? '-' : '+';
    return `${sign}${this.formatHourValue(Math.abs(value))}`;
  }

  static getHoursTooltip(
    preview: AssistancePlanPreviewDto,
    period: AssistancePlanStatusPeriod = 'year'
  ): string {
    const executedHours = this.getExecutedHours(preview, period);
    const periodLabel = period === 'assistancePlan' ? 'Dieser Hilfeplan bis heute' : 'Dieses Jahr bis heute';
    if (this.isCorridorPlan(preview)) {
      return `${periodLabel}\nBewilligt von: ${this.getApprovedHoursFrom(preview, period)}\nBewilligt bis: ${this.getApprovedHoursTill(preview, period)}\nGeleistet: ${executedHours}`;
    }

    return `${periodLabel}\nBewilligt: ${this.getApprovedHours(preview, period)}\nGeleistet: ${executedHours}`;
  }

  static getApprovedHours(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursThisAssistancePlan : preview.approvedHoursThisYear;
  }

  static getExecutedHours(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.executedHoursThisAssistancePlan : preview.executedHoursThisYear;
  }

  static getApprovedHoursFrom(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursThisAssistancePlanFrom : preview.approvedHoursThisYearFrom;
  }

  static getApprovedHoursTill(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursThisAssistancePlanTill : preview.approvedHoursThisYearTill;
  }

  static getApprovedHoursLeft(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursLeftThisAssistancePlan : preview.approvedHoursLeftThisYear;
  }

  static formatHourValue(value: number): string {
    return value.toLocaleString('de-DE', {maximumFractionDigits: 2});
  }

  private static getCorridorExecutedHoursPercent(
    preview: AssistancePlanPreviewDto,
    period: AssistancePlanStatusPeriod
  ): number {
    const approvedFrom = this.convertTimeDoubleToMinutes(this.getApprovedHoursFrom(preview, period));
    const approvedTill = this.convertTimeDoubleToMinutes(this.getApprovedHoursTill(preview, period));
    const executed = this.convertTimeDoubleToMinutes(this.getExecutedHours(preview, period));

    if (approvedFrom <= 0 || approvedTill <= 0) {
      return 0;
    }

    if (executed < approvedFrom) {
      return this.clampPercent((executed * 40) / approvedFrom);
    }

    if (executed > approvedTill) {
      return this.clampPercent(60 + (executed * 40) / (approvedFrom + approvedTill));
    }

    const corridorWidth = approvedTill - approvedFrom;
    if (corridorWidth <= 0) {
      return 40;
    }

    return this.clampPercent(40 + ((executed - approvedFrom) * 20) / corridorWidth);
  }

  private static clampPercent(value: number): number {
    return Math.max(0, Math.min(100, Number(value.toFixed(1))));
  }

  /** Hour values are stored as time doubles, i.e. 1.30 means one hour and 30 minutes. */
  private static convertTimeDoubleToMinutes(value: number): number {
    const sign = value < 0 ? -1 : 1;
    const absoluteValue = Math.abs(value);
    const hours = Math.trunc(absoluteValue);
    const minutes = Math.round((absoluteValue - hours) * 100);
    return sign * (hours * 60 + minutes);
  }
}
