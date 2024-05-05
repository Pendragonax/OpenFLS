import { Injectable } from '@angular/core';
import {Base} from "./base.service";
import {ContingentDto} from "../dtos/contingent-dto.model";
import {combineLatest, Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {EmployeeDto} from "../dtos/employee-dto.model";
import {InstitutionDto} from "../dtos/institution-dto.model";
import {map} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";
import {EmployeeService} from "./employee.service";
import {InstitutionService} from "./institution.service";
import {UserService} from "./user.service";
import {ContingentEvaluationDto} from "../domains/contingent-overviews/dtos/contingent-evaluation-dto.model";

@Injectable({
  providedIn: 'root'
})
export class ContingentsService extends Base<ContingentDto> {
  url = "contingents";

  constructor(
    protected override http: HttpClient,
    protected employeeService: EmployeeService,
    protected institutionService: InstitutionService,
    protected userService: UserService
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
    });
  }

  getByEmployeeId(id: Number): Observable<ContingentDto[]> {
    return this.http
      .get<ContingentDto[]>(`${environment.api_url}${this.url}/employee/${id}`)
  }

  getByInstitutionId(id: Number): Observable<ContingentDto[]> {
    return this.http
      .get<ContingentDto[]>(`${environment.api_url}${this.url}/institution/${id}`)
  }

  getCombinationByEmployeeId(id: number): Observable<[EmployeeDto, InstitutionDto, ContingentDto, boolean][]> {
    return combineLatest([
      this.employeeService.getById(id),
      this.institutionService.allValues$,
      this.getByEmployeeId(id),
      this.userService.leadingInstitutions$,
      this.userService.isAdmin$]
    ).pipe(map(([employee, institutions, contingents, leadingIds, isAdmin]) => {
      return contingents.map<[EmployeeDto, InstitutionDto, ContingentDto, boolean]>(contingent => {
        return [
          employee,
          institutions.find(x => x.id === contingent.institutionId) ?? new InstitutionDto(),
          contingent,
          leadingIds.some(x => x == contingent.institutionId) || isAdmin
        ]
      })
    }))
  }

  getCombinationByInstitutionId(id: number): Observable<[EmployeeDto, InstitutionDto, ContingentDto, boolean][]> {
    return combineLatest([
      this.institutionService.getById(id),
      this.employeeService.allValues$,
      this.getByInstitutionId(id),
      this.userService.leadingInstitutions$,
      this.userService.isAdmin$]
    ).pipe(map(([institution, employees, contingents, leadingIds, isAdmin]) => {
      return contingents.map<[EmployeeDto, InstitutionDto, ContingentDto, boolean]>(contingent => {
        return [
          employees.find(x => x.id == contingent.employeeId) ?? new EmployeeDto(),
          institution,
          contingent,
          leadingIds.some(x => x == contingent.institutionId) || isAdmin
        ]
      })
    }))
  }
}
