import { Injectable } from '@angular/core';
import {Observable, tap} from "rxjs";
import {environment} from "../../../environments/environment";
import {EmployeeDto} from "../dtos/employee-dto.model";
import {Base} from "./base.service";
import { HttpClient } from "@angular/common/http";
import {EmployeeSolo} from "../dtos/employee-solo.projection";

@Injectable({
  providedIn: 'root'
})
export class EmployeeService extends Base<EmployeeDto>{
  url = "employees";

  constructor(
    protected override http: HttpClient
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
    });
  }

  getAllProjections(): Observable<EmployeeSolo[]> {
    return this.http
      .get<EmployeeSolo[]>(`${environment.api_url}${this.url}/projections`);
  }

  updateRole(id: Number, role: Number): Observable<EmployeeDto> {
    return this.http
      .put<EmployeeDto>(`${environment.api_url}${this.url}/${id}/${role}`, null)
      .pipe(tap(() => this.initialLoad()));
  }

  resetPassword(id: number): Observable<EmployeeDto> {
    return this.http
      .put<EmployeeDto>(`${environment.api_url}${this.url}/reset_password/${id}`, null)
  }

  addAssistancePlanFavorite(id: number) {
    return this.http
      .post(`${environment.api_url}${this.url}/assistance_plan/favorite/${id}`, null)
  }

  deleteAssistancePlanFavorite(id: number) {
    return this.http
      .delete(`${environment.api_url}${this.url}/assistance_plan/favorite/${id}`)
  }
}
