import {Injectable} from '@angular/core';
import { HttpClient } from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../../environments/environment";
import {AssistancePlansAnalysisMonthDto} from "../dtos/assistance-plans-analysis-month-dto";
import {DateService} from "../../../shared/services/date.service";
import {Converter} from "../../../shared/services/converter.helper";

@Injectable({
  providedIn: 'root'
})
export class AssistancePlanAnalysisService {
  url = "analysis/assistance_plans";

  constructor(
    protected http: HttpClient,
    private dateService: DateService,
    private converter: Converter
  ) { }

  convertToArray(analysis: AssistancePlansAnalysisMonthDto): any[][] {
    let rows: any[][] = []

    rows.push(["Klientenname", "Start", "Ende", "genehmigte Stunden",
      "geleistete Stunden", "fehlende Stunden", "geleistet in %"])

    let start = new Date(analysis.year, analysis.month - 1, 1)
    let end = new Date(analysis.year, analysis.month, 0)

    rows.push([
      "Gesamt",
      this.converter.getLocalDateString(start.toLocaleString()),
      this.converter.getLocalDateString(end.toLocaleString()),
      analysis.approvedHours,
      analysis.executedHours,
      analysis.missingHours,
      analysis.executedPercent])

    for (const assistancePlan of analysis.assistancePlanAnalysis) {
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

  getByYearAndMonthAndInstitutionIdAndSponsorIdAndHourTypeId(year: number,
                                                             month: number,
                                                             institutionId: number,
                                                             sponsorId: number,
                                                             hourTypeId: number): Observable<AssistancePlansAnalysisMonthDto> {
    return this.http
      .get<AssistancePlansAnalysisMonthDto>(
        `${environment.api_url}${this.url}/institution/sponsor/hour_type/${year}/${month}/${institutionId}/${sponsorId}/${hourTypeId}`)
  }

  getByYearAndMonthAndInstitutionIdAndHourTypeId(year: number,
                                                 month: number,
                                                 institutionId: number,
                                                 hourTypeId: number): Observable<AssistancePlansAnalysisMonthDto> {
    return this.http
      .get<AssistancePlansAnalysisMonthDto>(
        `${environment.api_url}${this.url}/institution/hour_type/${year}/${month}/${institutionId}/${hourTypeId}`)
  }

  getByYearAndMonthAndInstitutionId(year: number,
                                    month: number,
                                    institutionId: number,): Observable<AssistancePlansAnalysisMonthDto> {
    return this.http
      .get<AssistancePlansAnalysisMonthDto>(
        `${environment.api_url}${this.url}/institution/${year}/${month}/${institutionId}`)
  }
}
