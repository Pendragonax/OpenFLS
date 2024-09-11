import {AssistancePlanHourResponseDto} from "./assistance-plan-hour-response-dto.model";
import {GoalResponseDto} from "./goal-response-dto.model";

export class AssistancePlanResponseDto {
  id: number = 0;
  start: string = Date.now().toLocaleString();
  end: string = Date.now().toLocaleString();
  clientId: number = 0;
  clientFirstName: string = "";
  clientLastName: string = "";
  institutionId: number = 0;
  institutionName: string = "";
  sponsorId: number = 0;
  sponsorName: string = "";
  hours: AssistancePlanHourResponseDto[] = [];
  goals: GoalResponseDto[] = [];
}
