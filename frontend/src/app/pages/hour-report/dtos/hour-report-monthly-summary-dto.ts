import {HourReportMonthlySummaryRowDto} from "./hour-report-monthly-summary-row-dto";

export class HourReportMonthlySummaryDto {
  year: number = 0
  month: number = 0
  approvedHours: number = 0
  executedHours: number = 0
  executedPercent: number = 0
  missingHours: number = 0
  rows: HourReportMonthlySummaryRowDto[] = []
}
