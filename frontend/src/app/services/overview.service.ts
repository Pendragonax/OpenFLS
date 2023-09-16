import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ServiceDto} from "../dtos/service-dto.model";
import {environment} from "../../environments/environment";
import {OverviewAssistancePlan} from "../dtos/overview-assistance-plan.dto";

@Injectable({
  providedIn: 'root'
})
export class OverviewService {
  url = "/overviews"

  constructor(
    protected http: HttpClient) { }

  getExecutedHoursOverviewFromAssistancePlanByYearAndMonth(year: number,
                                                           hourTypeId: number,
                                                           areaId: number,
                                                           sponsorId: number): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/${year}/${hourTypeId}/${areaId}/${sponsorId}/EXECUTED_HOURS`)
  }
}
