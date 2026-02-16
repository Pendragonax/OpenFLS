import {GoalDto} from "./goal-dto.model";
import {AssistancePlanHourDto} from "./assistance-plan-hour-dto.model";

export class AssistancePlanDto {
  id: number = 0;
  start: string = Date.now().toLocaleString();
  end: string = Date.now().toLocaleString();
  clientId: number = 0;
  institutionId: number = 0;
  institutionName: string = '';
  sponsorId: number = 0;
  hours: AssistancePlanHourDto[] = [];
  goals: GoalDto[] = [];
}
