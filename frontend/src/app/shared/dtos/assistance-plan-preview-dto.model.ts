import {AssistancePlanHourMode} from "./assistance-plan-hour-mode.model";

export class AssistancePlanPreviewDto {
  id: number = 0;
  clientId: number = 0;
  institutionId: number = 0;
  sponsorId: number = 0;
  clientFirstname: string = '';
  clientLastname: string = '';
  institutionName: string = '';
  sponsorName: string = '';
  start: string = '';
  end: string = '';
  clientArchived: boolean = false;
  isActive: boolean = false;
  isFavorite: boolean = false;
  hasIllegalHours: boolean = false;
  hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT;
  approvedHoursFrom: number = 0;
  approvedHoursTo: number = 0;
  approvedHoursPerWeek: number = 0;
  approvedHoursThisYearFrom: number = 0;
  approvedHoursThisYearTill: number = 0;
  approvedHoursThisYear: number = 0;
  executedHoursThisYear: number = 0;
}
