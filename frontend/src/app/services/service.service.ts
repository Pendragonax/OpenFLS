import { Injectable } from '@angular/core';
import {Base} from "./base.service";
import {ServiceDto} from "../dtos/service-dto.model";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Converter} from "../shared/converter.helper";
import {ServiceTimeDto} from "../dtos/service-time-dto.model";

@Injectable({
  providedIn: 'root'
})
export class ServiceService extends Base<ServiceDto> {
  url = "services";

  constructor(
    protected override http: HttpClient,
    protected converter: Converter
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
  }

  getByEmployeeAndDate(employeeId: number, date: Date): Observable<ServiceDto[]> {
    return this.http
      .get<ServiceDto[]>(`${environment.api_url}${this.url}/employee/${employeeId}/${this.converter.formatDate(date)}`)
  }

  getByClientAndDate(clientId: number, date: Date): Observable<ServiceDto[]> {
    return this.http
      .get<ServiceDto[]>(`${environment.api_url}${this.url}/client/${clientId}/${this.converter.formatDate(date)}`)
  }

  getTimesByEmployeeAndStartEnd(employeeId: number, start: Date, end: Date): Observable<ServiceTimeDto> {
    return this.http
      .get<ServiceTimeDto>(`${environment.api_url}${this.url}/times/${employeeId}/${this.converter.formatDate(start)}/${this.converter.formatDate(end)}`)
  }

  getByAssistancePlan(assistancePlanId: number): Observable<ServiceDto[]> {
    return this.http
      .get<ServiceDto[]>(`${environment.api_url}${this.url}/assistance_plan/${assistancePlanId}`)
  }

  getCountByEmployeeId(employeeId: number): Observable<number> {
    return this.http
      .get<number>(`${environment.api_url}${this.url}/count/employee/${employeeId}`)

  }
}
