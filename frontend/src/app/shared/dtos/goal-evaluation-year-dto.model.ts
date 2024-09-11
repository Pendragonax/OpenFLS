import {EvaluationMonthDto} from "./evaluation-month-dto.model";

export class GoalEvaluationYearDto {
  goalId: number = 0
  title: string = ""
  months: EvaluationMonthDto[] = []
}
