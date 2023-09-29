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

  getOverviewFromAssistancePlanByYear(year: number,
                                      hourTypeId: number | null,
                                      areaId: number | null,
                                      sponsorId: number | null,
                                      overviewType: EOverviewType): Observable<OverviewAssistancePlan[]> {
    return this.http
      .get<OverviewAssistancePlan[]>(`${environment.api_url}${this.url}/year/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EOverviewType, overviewType)}`)
  }

  getOverviewCSVFromAssistancePlanByYear(year: number,
                                         hourTypeId: number | null,
                                         areaId: number | null,
                                         sponsorId: number | null,
                                         overviewType: EOverviewType): Observable<any> {
    return this.http
      .get(`${environment.api_url}${this.url}/year/csv/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EOverviewType, overviewType)}`, {responseType: 'blob', observe: 'response'})
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

  getOverviewCSVFromAssistancePlanByYearAndMonth(year: number,
                                                 month: number,
                                                 hourTypeId: number | null,
                                                 areaId: number | null,
                                                 sponsorId: number | null,
                                                 overviewType: EOverviewType): Observable<any> {
    return this.http
      .get(`${environment.api_url}${this.url}/month/csv/${year}/${month}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EOverviewType, overviewType)}`, {responseType: 'blob', observe: 'response'})
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
