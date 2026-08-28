import {AssistancePlanHourMode} from '../../../shared/dtos/assistance-plan-hour-mode.model';

export class AssistancePlanAnalysisMonthDto {
  assistancePlanId: number = 0
  start: string = ""
  end: string = ""
  clientFirstName: string = ""
  clientLastName: string = ""
  hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT
  year: number = 0
  month: number = 0
  approvedHours: number = 0
  executedHours: number = 0
  executedPercent: number = 0
  missingHours: number = 0
}
