import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {OverviewAssistancePlan} from "../dtos/overview-assistance-plan.dto";
import {EOverviewType} from "../enums/EOverviewType";

@Injectable({
  providedIn: 'root'
})
export class OverviewService {
  url = "/overviews"

  constructor(
    protected http: HttpClient) {
  }

  private EXECUTED_HOURS_TYPE_URL_STRING = `EXECUTED_HOURS`;
  private APPROVED_HOURS_TYPE_URL_STRING = `APPROVED_HOURS`;

  getOverviewFromAssistancePlanByYear(year: number,
                                      hourTypeId: number | null,
                                      areaId: number | null,
                                      sponsorId: number | null,
                                      overviewType: EOverviewType): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/year/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EOverviewType, overviewType)}`)
  }

  getOverviewFromAssistancePlanByYearAndMonth(year: number,
                                              month: number,
                                              hourTypeId: number | null,
                                              areaId: number | null,
                                              sponsorId: number | null,
                                              overviewType: EOverviewType): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/month/${year}/${month}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EOverviewType, overviewType)}`)
  }

  getExecutedHoursOverviewFromAssistancePlanByYear(year: number,
                                                   hourTypeId: number | null,
                                                   areaId: number | null,
                                                   sponsorId: number | null): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/year/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.EXECUTED_HOURS_TYPE_URL_STRING}`)
  }

  getExecutedHoursOverviewFromAssistancePlanByYearAndMonth(year: number,
                                                           month: number,
                                                           hourTypeId: number | null,
                                                           areaId: number | null,
                                                           sponsorId: number | null): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/month/${year}/${month}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.EXECUTED_HOURS_TYPE_URL_STRING}`)
  }

  getApprovedHoursOverviewFromAssistancePlanByYear(year: number,
                                                   hourTypeId: number | null,
                                                   areaId: number | null,
                                                   sponsorId: number | null): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/year/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.APPROVED_HOURS_TYPE_URL_STRING}`)
  }

  getApprovedHoursOverviewFromAssistancePlanByYearAndMonth(year: number,
                                                           month: number,
                                                           hourTypeId: number | null,
                                                           areaId: number | null,
                                                           sponsorId: number | null): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/month/${year}/${month}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.APPROVED_HOURS_TYPE_URL_STRING}`)
  }

  private getEnumName<T>(enumObj: T, value: T[keyof T]): keyof T | string {
    for (const key in enumObj) {
      if (enumObj[key] === value) {
        return key as keyof T;
      }
    }
    return "";
  }
}
