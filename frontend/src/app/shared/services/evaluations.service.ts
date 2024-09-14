import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {EvaluationRequestDto} from "../dtos/evaluation-request-dto.model";
import { HttpClient } from "@angular/common/http";
import {EvaluationDto} from "../dtos/evaluation-dto.model";
import {EvaluationYearDto} from "../dtos/evaluation-year-dto.model";

@Injectable({
  providedIn: 'root'
})
export class EvaluationsService {
  url = "evaluations"

  constructor(
    protected http: HttpClient) { }

  create(value: EvaluationRequestDto) : Observable<EvaluationDto> {
    return this.http
      .post<EvaluationDto>(`${environment.api_url}${this.url}`, value)
  }

  update(value: EvaluationRequestDto) : Observable<EvaluationDto> {
    return this.http
      .put<EvaluationDto>(`${environment.api_url}${this.url}`, value)
  }

  delete(id: number) : Observable<EvaluationDto> {
    return this.http
      .delete<EvaluationDto>(`${environment.api_url}${this.url}/${id}`)
  }

  getAll(): Observable<EvaluationDto[]> {
    return this.http
      .get<EvaluationDto[]>(`${environment.api_url}${this.url}`);
  }

  getById(id: number): Observable<EvaluationDto> {
    return this.http
      .get<EvaluationDto>(`${environment.api_url}${this.url}/${id}`);
  }

  getByAssistancePlanIdAndYear(assistancePlanId: number, year: number): Observable<EvaluationYearDto> {
    return this.http
      .get<EvaluationYearDto>(`${environment.api_url}${this.url}/assistance_plan/${assistancePlanId}/${year}`);
  }
}
