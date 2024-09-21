import {Base} from "./base.service";
import {AssistancePlanHourSolo} from "../projections/assistance-plan-hour-solo.projection";
import {Injectable} from "@angular/core";
import {Observable, tap} from "rxjs";
import {environment} from "../../../environments/environment";
import {AssistancePlanHour} from "../projections/assistance-plan-hour.projection";
import {HttpClient} from "@angular/common/http";
import {AssistancePlanHourDto} from "../dtos/assistance-plan-hour-dto.model";

@Injectable({
  providedIn: 'root'
})
export class AssistancePlanHourService {
  url = "assistance_plan_hours";

  constructor(private http: HttpClient) {
  }

  create(value: AssistancePlanHourDto): Observable<AssistancePlanHour> {
    return this.http
      .post<AssistancePlanHour>(`${environment.api_url}${this.url}`, value)
  }

  update(value: AssistancePlanHourDto): Observable<AssistancePlanHour> {
    return this.http
      .put<AssistancePlanHour>(`${environment.api_url}${this.url}`, value)
  }

  delete(id: number): Observable<AssistancePlanHourSolo> {
    return this.http
      .delete<AssistancePlanHourSolo>(`${environment.api_url}${this.url}/${id}`)
  }
}
