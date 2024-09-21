import {GoalTimeEvaluationDto} from "./goal-time-evaluation-dto.model";

export class GoalsTimeEvaluationDto {
  assistancePlanId: number = 0;
  executedHours: number[] = [];
  summedExecutedHours: number[] = [];
  approvedHours: number[] = [];
  summedApprovedHours: number[] = [];
  approvedHoursLeft: number[] = [];
  summedApprovedHoursLeft: number[] = [];
  goalTimeEvaluations: GoalTimeEvaluationDto[] = []
}
