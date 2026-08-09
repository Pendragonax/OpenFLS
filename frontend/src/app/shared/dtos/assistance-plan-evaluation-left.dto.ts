import {AssistancePlanHourMode} from "./assistance-plan-hour-mode.model";

export class AssistancePlanEvaluationLeftDto {
  assistancePlanId: number = 0;
  hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT;
  approvedHoursFrom: number = 0;
  approvedHoursTo: number = 0;
  hourTypeEvaluation: AssistancePlanHourTypeEvaluationLeftDto[] = [];
}

export class AssistancePlanHourTypeEvaluationLeftDto {
  hourTypeName: string = '';
  leftThisWeek: number = 0;
  leftThisMonth: number = 0;
  leftThisYear: number = 0;
  leftComplete: number = 0;
}
