export class ContingentEmployeeEvaluationDto {
  lastname: string = ""
  firstname: string = ""
  archived: boolean = false
  contingentHours: number[] = []
  executedHours: number[] = []
  executedPercent: number[] = []
  summedExecutedPercent: number[] = []
  missingHours: number[] = []
  absenceDays: number[] = []
}
