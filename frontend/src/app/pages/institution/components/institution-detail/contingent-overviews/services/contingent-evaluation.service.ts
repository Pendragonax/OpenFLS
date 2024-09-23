import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {ContingentEvaluationDto} from "../dtos/contingent-evaluation-dto.model";
import {environment} from "../../../../../../../environments/environment";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ContingentEvaluationService {
  url = "contingents";

  constructor(
    private http: HttpClient) { }

  getOverviewByInstitutionIdAndYear(institutionId: number, year: number): Observable<ContingentEvaluationDto> {
    return this.http
      .get<ContingentEvaluationDto>(`${environment.api_url}${this.url}/evaluations/institution/${institutionId}/${year}`)
  }
}
