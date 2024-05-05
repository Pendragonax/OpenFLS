import {AssistancePlanAnalysisMonthDto} from "./assistance-plan-analysis-month-dto";

export class AssistancePlansAnalysisMonthDto {
  year: number = 0
  month: number = 0
  approvedHours: number = 0
  executedHours: number = 0
  executedPercent: number = 0
  missingHours: number = 0
  assistancePlanAnalysis: AssistancePlanAnalysisMonthDto[] = []
}
