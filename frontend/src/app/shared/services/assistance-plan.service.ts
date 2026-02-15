import {Injectable} from '@angular/core';
import {AssistancePlanDto} from '../dtos/assistance-plan-dto.model';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable, ReplaySubject, tap} from 'rxjs';
import {AssistancePlanEvaluation} from '../dtos/assistance-plan-evaluation.model';
import {AssistancePlanEvaluationLeftDto} from '../dtos/assistance-plan-evaluation-left.dto';
import {AssistancePlan} from '../projections/assistance-plan.projection';
import {AssistancePlanCreateDto} from '../dtos/assistance-plan-create-dto.model';
import {AssistancePlanUpdateDto} from '../dtos/assistance-plan-update-dto.model';
import {AssistancePlanPreviewDto} from '../dtos/assistance-plan-preview-dto.model';
import {AssistancePlanExistingDto} from '../dtos/assistance-plan-existing-dto.model';

@Injectable({
  providedIn: 'root'
})
export class AssistancePlanService {
  allValues$: ReplaySubject<AssistancePlanDto[]> = new ReplaySubject<AssistancePlanDto[]>();
  allValues: AssistancePlanDto[] = [];
  url = 'assistance_plans';

  constructor(protected http: HttpClient) {}

  initialLoad() {}

  create(value: AssistancePlanCreateDto): Observable<AssistancePlanDto> {
    return this.http
      .post<AssistancePlanDto>(`${environment.api_url}${this.url}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  update(id: number, value: AssistancePlanDto): Observable<AssistancePlanDto> {
    return this.http
      .put<AssistancePlanDto>(`${environment.api_url}${this.url}/${id}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  updateWithCreateLikeDto(id: number, value: AssistancePlanUpdateDto): Observable<AssistancePlanDto> {
    return this.http
      .put<AssistancePlanDto>(`${environment.api_url}${this.url}/${id}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  delete(id: number): Observable<AssistancePlanDto> {
    return this.http
      .delete<AssistancePlanDto>(`${environment.api_url}${this.url}/${id}`)
      .pipe(tap(() => this.initialLoad()));
  }

  getAll(): Observable<AssistancePlanDto[]> {
    return this.http.get<AssistancePlanDto[]>(`${environment.api_url}${this.url}`);
  }

  getById(id: number): Observable<AssistancePlanDto> {
    return this.http.get<AssistancePlanDto>(`${environment.api_url}${this.url}/${id}`);
  }

  getProjectionById(id: number): Observable<AssistancePlan> {
    return this.http.get<AssistancePlan>(`${environment.api_url}${this.url}/projection/${id}`);
  }

  getByClientId(id: number): Observable<AssistancePlanDto[]> {
    return this.http.get<AssistancePlanDto[]>(`${environment.api_url}${this.url}/client/${id}`);
  }

  getIllegalByClientId(id: number): Observable<AssistancePlan[]> {
    return this.http.get<AssistancePlan[]>(`${environment.api_url}${this.url}/client/${id}/illegal`);
  }

  getByInstitutionId(id: number): Observable<AssistancePlanDto[]> {
    return this.http.get<AssistancePlanDto[]>(`${environment.api_url}${this.url}/institution/${id}`);
  }

  getIllegalByInstitutionId(id: number): Observable<AssistancePlan[]> {
    return this.http.get<AssistancePlan[]>(`${environment.api_url}${this.url}/institution/${id}/illegal`);
  }

  getBySponsorId(id: number): Observable<AssistancePlanDto[]> {
    return this.http.get<AssistancePlanDto[]>(`${environment.api_url}${this.url}/sponsor/${id}`);
  }

  getIllegalBySponsorId(id: number): Observable<AssistancePlan[]> {
    return this.http.get<AssistancePlan[]>(`${environment.api_url}${this.url}/sponsor/${id}/illegal`);
  }

  getPreviewByClientId(id: number): Observable<AssistancePlanPreviewDto[]> {
    return this.http.get<AssistancePlanPreviewDto[]>(`${environment.api_url}${this.url}/client/${id}/preview`);
  }

  getPreviewByInstitutionId(id: number): Observable<AssistancePlanPreviewDto[]> {
    return this.http.get<AssistancePlanPreviewDto[]>(`${environment.api_url}${this.url}/institution/${id}/preview`);
  }

  getPreviewBySponsorId(id: number): Observable<AssistancePlanPreviewDto[]> {
    return this.http.get<AssistancePlanPreviewDto[]>(`${environment.api_url}${this.url}/sponsor/${id}/preview`);
  }

  getPreviewByFavorites(): Observable<AssistancePlanPreviewDto[]> {
    return this.http.get<AssistancePlanPreviewDto[]>(`${environment.api_url}${this.url}/favorites/preview`);
  }

  getExistingByClientId(id: number): Observable<AssistancePlanExistingDto[]> {
    return this.http.get<AssistancePlanExistingDto[]>(`${environment.api_url}${this.url}/client/${id}/existing`);
  }

  getEvaluationById(id: number): Observable<AssistancePlanEvaluation> {
    return this.http.get<AssistancePlanEvaluation>(`${environment.api_url}${this.url}/eval/${id}`);
  }

  getEvaluationLeftById(id: number): Observable<AssistancePlanEvaluationLeftDto> {
    return this.http.get<AssistancePlanEvaluationLeftDto>(`${environment.api_url}${this.url}/eval/left/${id}`);
  }
}
