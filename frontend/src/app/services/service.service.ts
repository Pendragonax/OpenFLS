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

  getByEmployeeAndStartAndEnd(employeeId: number, start: Date, end: Date): Observable<ServiceDto[]> {
    return this.http
      .get<ServiceDto[]>(`${environment.api_url}${this.url}/employee/${employeeId}/${this.converter.formatDate(start)}/${this.converter.formatDate(end)}`)
  }

  getByClientAndDate(clientId: number, date: Date): Observable<ServiceDto[]> {
    return this.http
      .get<ServiceDto[]>(`${environment.api_url}${this.url}/client/${clientId}/${this.converter.formatDate(date)}`)
  }

  getByClientAndStartAndEnd(clientId: number, start: Date, end: Date): Observable<ServiceDto[]> {
    return this.http
      .get<ServiceDto[]>(`${environment.api_url}${this.url}/client/${clientId}/${this.converter.formatDate(start)}/${this.converter.formatDate(end)}`)
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

  getCountByClientId(clientId: number): Observable<number> {
    return this.http
      .get<number>(`${environment.api_url}${this.url}/count/client/${clientId}`)

  }

  getCountByAssistancePlanId(assistancePlanId: number): Observable<number> {
    return this.http
      .get<number>(`${environment.api_url}${this.url}/count/assistance_plan/${assistancePlanId}`)

  }

  getCountByGoalId(goalId: number): Observable<number> {
    return this.http
      .get<number>(`${environment.api_url}${this.url}/count/goal/${goalId}`)

  }
}
