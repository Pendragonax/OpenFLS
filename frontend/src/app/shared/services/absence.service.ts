import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject, tap} from "rxjs";
import {environment} from "../../../environments/environment";
import {CreateAbsenceDTO, EmployeeAbsenceDTO} from "../dtos/calendar-information-dto.model";
import {Converter} from "./converter.helper";

@Injectable({
  providedIn: 'root'
})
export class AbsenceService {
  allValues$: ReplaySubject<EmployeeAbsenceDTO> = new ReplaySubject<EmployeeAbsenceDTO>();
  url = "absences";


  constructor(
    private http: HttpClient,
    private converter: Converter
  ) {
  }

  initialLoad() {
    this.getForMe().subscribe(values => {
      this.allValues$.next(values);
    });
  }

  create(employeeId: number, date: Date) : Observable<EmployeeAbsenceDTO> {
    const value: CreateAbsenceDTO = {
      employeeId: employeeId,
      absenceDate: this.converter.formatDate(date)
    };
    return this.http
      .post<EmployeeAbsenceDTO>(`${environment.api_url}${this.url}`, value)
      .pipe(tap(() => this.initialLoad()));
  }

  remove(date: Date) : Observable<void> {
    return this.http
      .delete<void>(`${environment.api_url}${this.url}/${this.converter.formatDate(date)}`)
      .pipe(tap(() => this.initialLoad()));
  }

  getForMe(): Observable<EmployeeAbsenceDTO> {
    return this.http
      .get<EmployeeAbsenceDTO>(`${environment.api_url}${this.url}`);
  }
}
