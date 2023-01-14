import { Injectable } from '@angular/core';
import {Base} from "./base.service";
import {ServiceDto} from "../dtos/service-dto.model";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Converter} from "../shared/converter.helper";

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
}
