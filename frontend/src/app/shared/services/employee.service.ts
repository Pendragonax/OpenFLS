import { Injectable } from '@angular/core';
import {Observable, tap} from "rxjs";
import {environment} from "../../../environments/environment";
import {EmployeeDto} from "../dtos/employee-dto.model";
import {Base} from "./base.service";
import { HttpClient } from "@angular/common/http";
import {EmployeeSolo} from "../dtos/employee-solo.projection";
import {map} from "rxjs/operators";

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

  override getAll(includeArchived: boolean = false): Observable<EmployeeDto[]> {
    return this.http
      .get<EmployeeDto[]>(`${environment.api_url}${this.url}?includeArchived=${includeArchived}`);
  }

  getAllProjections(): Observable<EmployeeSolo[]> {
    return this.http
      .get<EmployeeSolo[]>(`${environment.api_url}${this.url}/projections`);
  }

  getActiveEmployeeSelections(): Observable<EmployeeSolo[]> {
    return this.getAll().pipe(
      map(values => values
        .filter(value => !value.archived && !value.inactive)
        .map(value => ({
          id: value.id,
          firstname: value.firstName,
          lastname: value.lastName,
          email: value.email,
          phonenumber: value.phonenumber,
          description: value.description,
          archived: value.archived
        }))
      )
    );
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
