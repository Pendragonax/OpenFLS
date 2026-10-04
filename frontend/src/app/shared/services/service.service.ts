import {Injectable} from '@angular/core';
import {Base} from "./base.service";
import {ServiceDto} from "../dtos/service-dto.model";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Converter} from "./converter.helper";
import {Service} from "../dtos/service.projection";
import {ClientAndDateRequestDto} from "../dtos/client-and-date-request-dto.model";
import {ClientAndDateResponseDto} from "../dtos/client-and-date-response-dto.model";

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

  getOutsideAssistancePlanPeriodByEmployeeId(employeeId: number): Observable<Service[]> {
    return this.http
      .get<Service[]>(`${environment.api_url}${this.url}/employee/${employeeId}/outside_assistance_plan_period`)
  }

  getByEmployeeAndStartAndEnd(employeeId: number, start: Date, end: Date): Observable<Service[]> {
    return this.http
      .get<Service[]>(`${environment.api_url}${this.url}/employee/${employeeId}/${this.converter.formatDate(start)}/${this.converter.formatDate(end)}`)
  }

  getOutsideAssistancePlanPeriodByInstitutionId(institutionId: number): Observable<Service[]> {
    return this.http
      .get<Service[]>(`${environment.api_url}${this.url}/institution/${institutionId}/outside_assistance_plan_period`)
  }

  getByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(institutionId: number, employeeId: number, clientId: number, start: Date, end: Date): Observable<Service[]> {
    return this.http
      .get<Service[]>(`${environment.api_url}${this.url}/institution/${institutionId}/employee/${employeeId}/client/${clientId}/${this.converter.formatDate(start)}/${this.converter.formatDate(end)}`)
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

  getClientAndDateServices(clientId: number, date: Date): Observable<ClientAndDateResponseDto> {
    const payload: ClientAndDateRequestDto = {
      clientId,
      date: this.converter.formatDate(date)
    };

    return this.http.post<ClientAndDateResponseDto>(
      `${environment.api_url}${this.url}/client-and-date`,
      payload
    );
  }
}
