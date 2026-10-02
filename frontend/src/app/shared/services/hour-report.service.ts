import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {HourReportRow} from "../dtos/hour-report-row.dto";
import {EHourReportType} from "../../pages/hour-report/enums/EHourReportType";

@Injectable({
  providedIn: 'root'
})
export class HourReportService {
  url = "hour_reports"

  constructor(
    protected http: HttpClient) {
  }

  getHourReportByYear(year: number,
                      hourTypeId: number | null,
                      areaId: number | null,
                      sponsorId: number | null,
                      valueType: EHourReportType): Observable<HourReportRow[]> {
    return this.http
      .get<HourReportRow[]>(`${environment.api_url}${this.url}/year/${year}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EHourReportType, valueType)}`)
  }

  getHourReportByYearAndMonth(year: number,
                              month: number,
                              hourTypeId: number | null,
                              areaId: number | null,
                              sponsorId: number | null,
                              valueType: EHourReportType): Observable<HourReportRow[]> {
    return this.http
      .get<HourReportRow[]>(`${environment.api_url}${this.url}/month/${year}/${month}/${hourTypeId}/${areaId ?? 0}/${sponsorId ?? 0}/${this.getEnumName(EHourReportType, valueType)}`)
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
