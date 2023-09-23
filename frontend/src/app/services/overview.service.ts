import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {OverviewAssistancePlan} from "../dtos/overview-assistance-plan.dto";

@Injectable({
  providedIn: 'root'
})
export class OverviewService {
  url = "/overviews"

  constructor(
    protected http: HttpClient) { }

  getExecutedHoursOverviewFromAssistancePlanByYear(year: number,
                                                   hourTypeId: number | null,
                                                   areaId: number | null,
                                                   sponsorId: number | null): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/year/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/EXECUTED_HOURS`)
  }

  getExecutedHoursOverviewFromAssistancePlanByYearAndMonth(year: number,
                                                           month: number,
                                                           hourTypeId: number | null,
                                                           areaId: number | null,
                                                           sponsorId: number | null): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/month/${year}/${month}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/EXECUTED_HOURS`)
  }
}
