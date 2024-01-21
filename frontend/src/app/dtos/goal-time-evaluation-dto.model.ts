export class GoalTimeEvaluationDto {
  id: number = 0
  title: string = ""
  description: string = ""
  executedHours: number[] = []
  summedExecutedHours: number[] = []
  approvedHours: number[] = []
  summedApprovedHours: number[] = []
  approvedHoursLeft: number[] = []
  summedApprovedHoursLeft: number[] = []
}
