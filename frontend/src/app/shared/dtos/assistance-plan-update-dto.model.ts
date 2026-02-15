import {
  AssistancePlanCreateDto,
  AssistancePlanCreateGoalDto,
  AssistancePlanCreateHourDto
} from './assistance-plan-create-dto.model';

export class AssistancePlanUpdateDto extends AssistancePlanCreateDto {
  id: number = 0;
  override hours: AssistancePlanUpdateHourDto[] = [];
  override goals: AssistancePlanUpdateGoalDto[] = [];
}

export class AssistancePlanUpdateHourDto extends AssistancePlanCreateHourDto {
  override id: number = 0;
  override assistancePlanId: number = 0;
}

export class AssistancePlanUpdateGoalDto extends AssistancePlanCreateGoalDto {
  override id: number = 0;
  override hours: AssistancePlanUpdateGoalHourDto[] = [];
}

export class AssistancePlanUpdateGoalHourDto extends AssistancePlanCreateHourDto {
  override id: number = 0;
  override goalHourId: number = 0;
}
