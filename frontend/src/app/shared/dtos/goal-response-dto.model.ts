import {GoalHourDto} from "./goal-hour-dto.model";
import {GoalHourResponseDto} from "./goal-hour-response-dto.model";

export class GoalResponseDto {
  id: number = 0;
  title: string = "";
  description: string = "";
  assistancePlanId: number = 0;
  institutionId: number | null = null;
  institutionName: string = ""
  hours: GoalHourResponseDto[] = [];
}
