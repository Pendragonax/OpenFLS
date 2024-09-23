import { Injectable } from '@angular/core';
import {GoalsTimeEvaluationDto} from "../dtos/goals-time-evaluation-dto.model";
import {environment} from "../../../environments/environment";
import {Observable} from "rxjs";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class GoalTimeEvaluationService {

  url = "goal_evaluation";

  constructor(
    protected http: HttpClient) { }

  initialLoad() {
  }

  getByYear(assistancePlanId: number, hourTypeId: number, year: number): Observable<GoalsTimeEvaluationDto> {
    return this.http
      .get<GoalsTimeEvaluationDto>(`${environment.api_url}${this.url}/${assistancePlanId}/${hourTypeId}/${year}`)
  }

}
