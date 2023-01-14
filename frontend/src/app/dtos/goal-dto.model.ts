import {GoalHourDto} from "./goal-hour-dto.model";

export class GoalDto {
  id: number = 0;
  title: string = "";
  description: string = "";
  assistancePlanId: number = 0;
  institutionId: number | null = null;
  hours: GoalHourDto[] = [];
}
