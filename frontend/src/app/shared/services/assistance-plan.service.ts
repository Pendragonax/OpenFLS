import {Injectable} from '@angular/core';
import {AssistancePlanDto} from '../dtos/assistance-plan-dto.model';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable, ReplaySubject, tap} from 'rxjs';
import {AssistancePlanHoursLeftDto} from '../dtos/assistance-plan-hours-left.dto';
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

  getEditById(id: number): Observable<AssistancePlanDto> {
    return this.http.get<AssistancePlanDto>(`${environment.api_url}${this.url}/${id}/edit`);
  }

  getDetailById(id: number): Observable<AssistancePlan> {
    return this.http.get<AssistancePlan>(`${environment.api_url}${this.url}/${id}/detail`);
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

  getHoursLeftById(id: number): Observable<AssistancePlanHoursLeftDto> {
    return this.http.get<AssistancePlanHoursLeftDto>(`${environment.api_url}${this.url}/${id}/hours_left`);
  }
}
