
export class AssistancePlanCreateDto {
  start: string = Date.now().toLocaleString();
  end: string = Date.now().toLocaleString();
  clientId: number = 0;
  institutionId: number = 0;
  sponsorId: number = 0;
  hours: AssistancePlanCreateHourDto[] = [];
  goals: AssistancePlanCreateGoalDto[] = [];
}

export class AssistancePlanCreateHourDto {
  weeklyMinutes: number = 0;
  hourTypeId: number = 0;
}

export class AssistancePlanCreateGoalDto {
  title: string = "";
  description: string = "";
  assistancePlanId: number = 0;
  institutionId: number | null = null;
  hours: AssistancePlanCreateHourDto[] = [];
}
