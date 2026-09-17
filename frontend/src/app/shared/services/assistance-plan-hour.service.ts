import {AssistancePlanHourResponseDto} from "../dtos/assistance-plan-hour-response-dto.model";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {AssistancePlanHourDto} from "../dtos/assistance-plan-hour-dto.model";

@Injectable({
  providedIn: 'root'
})
export class AssistancePlanHourService {
  url = "assistance_plan_hours";

  constructor(private http: HttpClient) {
  }

  create(value: AssistancePlanHourDto): Observable<AssistancePlanHourResponseDto> {
    return this.http
      .post<AssistancePlanHourResponseDto>(`${environment.api_url}${this.url}`, value)
  }

  update(value: AssistancePlanHourDto): Observable<AssistancePlanHourResponseDto> {
    return this.http
      .put<AssistancePlanHourResponseDto>(`${environment.api_url}${this.url}`, value)
  }

  delete(id: number): Observable<AssistancePlanHourResponseDto> {
    return this.http
      .delete<AssistancePlanHourResponseDto>(`${environment.api_url}${this.url}/${id}`)
  }
}
