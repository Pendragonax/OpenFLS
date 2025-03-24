import {GoalDto} from "./goal-dto.model";
import {CategoryDto} from "./category-dto.model";

export class ServiceDto {
  id: number = 0;
  start: string = Date.now().toString();
  end: string = Date.now().toString();
  title: string = "";
  content: string = "";
  minutes: number = 0;
  unfinished: boolean = false;
  groupService: boolean = false;
  employeeId: number = 0;
  clientId: number = 0;
  institutionId: number = 0;
  assistancePlanId: number = 0;
  hourTypeId: number = 0;
  goals: GoalDto[] = [];
  categorys: CategoryDto[] = [];
}
