import {Injectable} from '@angular/core';
import { HttpClient } from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../../environments/environment";
import {HourReportMonthlySummaryDto} from "../dtos/hour-report-monthly-summary-dto";
import {DateService} from "../../../shared/services/date.service";
import {Converter} from "../../../shared/services/converter.helper";

@Injectable({
  providedIn: 'root'
})
export class HourReportMonthlySummaryService {
  url = "hour_reports";

  constructor(
    protected http: HttpClient,
    private dateService: DateService,
    private converter: Converter
  ) { }

  convertToArray(summary: HourReportMonthlySummaryDto): any[][] {
    let rows: any[][] = []

    rows.push(["Klientenname", "Start", "Ende", "genehmigte Stunden",
      "geleistete Stunden", "fehlende Stunden", "geleistet in %"])

    let start = new Date(summary.year, summary.month - 1, 1)
    let end = new Date(summary.year, summary.month, 0)

    rows.push([
      "Gesamt",
      this.converter.getLocalDateString(start.toLocaleString()),
      this.converter.getLocalDateString(end.toLocaleString()),
      summary.approvedHours,
      summary.executedHours,
      summary.missingHours,
      summary.executedPercent])

    for (const assistancePlan of summary.rows) {
      rows.push([
        `${assistancePlan.clientLastName}, ${assistancePlan.clientFirstName}`,
        this.converter.getLocalDateString(assistancePlan.start),
        this.converter.getLocalDateString(assistancePlan.end),
        assistancePlan.approvedHours,
        assistancePlan.executedHours,
        assistancePlan.missingHours,
        assistancePlan.executedPercent])
    }

    return rows
  }

  getMonthlySummary(year: number,
                                                             month: number,
                                                             institutionId: number,
                                                             sponsorId: number,
                                                             hourTypeId: number): Observable<HourReportMonthlySummaryDto> {
    return this.http
      .get<HourReportMonthlySummaryDto>(
        `${environment.api_url}${this.url}/month_summary/${year}/${month}/${institutionId}/${sponsorId}/${hourTypeId}`)
  }
}
