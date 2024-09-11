import {EvaluationDto} from "./evaluation-dto.model";

export class EvaluationMonthDto {
  month: number = 0;
  assistancePlanActive: boolean = false;
  evaluation: EvaluationDto | null = null
}
