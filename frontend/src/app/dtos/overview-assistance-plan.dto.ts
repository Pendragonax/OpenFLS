import {AssistancePlanDto} from "./assistance-plan-dto.model";
import {ClientDto} from "./client-dto.model";

export class OverviewAssistancePlan {
  assistancePlanDto: AssistancePlanDto | null = null;
  clientDto: ClientDto | null = null;
  values: number[] = [];
}
