import {EvaluationMonthDto} from "./evaluation-month-dto.model";
import {GoalEvaluationYearDto} from "./goal-evaluation-year-dto.model";

export class EvaluationYearDto {
  id: number = 0;
  values: GoalEvaluationYearDto[] = [];
}
