import {AssistancePlanDto} from "../dtos/assistance-plan-dto.model";

export class AssistancePlanView {
  dto: AssistancePlanDto = new AssistancePlanDto();
  editable: boolean = false;
  favorite: boolean = false;
  illegal: boolean = false;
}
