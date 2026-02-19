
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
  id?: number;
  weeklyMinutes: number = 0;
  hourTypeId: number = 0;
  assistancePlanId?: number;
  goalHourId?: number;
}

export class AssistancePlanCreateGoalDto {
  id?: number;
  title: string = "";
  description: string = "";
  assistancePlanId: number = 0;
  institutionId: number | null = null;
  hours: AssistancePlanCreateHourDto[] = [];
}
