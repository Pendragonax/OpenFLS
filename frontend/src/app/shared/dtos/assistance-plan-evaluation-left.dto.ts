export class AssistancePlanEvaluationLeftDto {
  assistancePlanId: number = 0;
  hourTypeEvaluation: AssistancePlanHourTypeEvaluationLeftDto[] = [];
}

export class AssistancePlanHourTypeEvaluationLeftDto {
  hourTypeName: string = '';
  leftThisWeek: number = 0;
  leftThisMonth: number = 0;
  leftThisYear: number = 0;
}
