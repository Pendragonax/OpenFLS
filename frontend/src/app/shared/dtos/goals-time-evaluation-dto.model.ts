import {GoalTimeEvaluationDto} from "./goal-time-evaluation-dto.model";
import {AssistancePlanHourMode} from "./assistance-plan-hour-mode.model";

export class GoalsTimeEvaluationDto {
  assistancePlanId: number = 0;
  hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT;
  executedHours: number[] = [];
  summedExecutedHours: number[] = [];
  approvedHours: number[] = [];
  summedApprovedHours: number[] = [];
  approvedHoursLeft: number[] = [];
  summedApprovedHoursLeft: number[] = [];
  goalTimeEvaluations: GoalTimeEvaluationDto[] = [];
  // Nur bei hourMode === CORRIDOR: drei Hilfeplan-Zeilen (Untergrenze, Obergrenze, Durchschnitt).
  corridorAssistancePlanEvaluations: GoalTimeEvaluationDto[] = [];
}
