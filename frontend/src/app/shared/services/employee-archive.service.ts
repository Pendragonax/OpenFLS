import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {EmployeeArchiveActionRequest} from "../dtos/employee-archive-action-request.model";
import {EmployeeArchiveHistoryEntryReadDto} from "../dtos/employee-archive-history-entry-read-dto.model";

@Injectable({
  providedIn: 'root'
})
export class EmployeeArchiveService {
  private readonly url = 'employees';

  constructor(private http: HttpClient) {
  }

  getArchiveHistoryById(id: number): Observable<EmployeeArchiveHistoryEntryReadDto[]> {
    return this.http.get<EmployeeArchiveHistoryEntryReadDto[]>(`${environment.api_url}${this.url}/${id}/archive/history`);
  }

  archive(id: number, request: EmployeeArchiveActionRequest): Observable<EmployeeArchiveHistoryEntryReadDto> {
    return this.http.post<EmployeeArchiveHistoryEntryReadDto>(`${environment.api_url}${this.url}/${id}/archive`, request);
  }

  reactivate(id: number, request: EmployeeArchiveActionRequest): Observable<EmployeeArchiveHistoryEntryReadDto> {
    return this.http.post<EmployeeArchiveHistoryEntryReadDto>(`${environment.api_url}${this.url}/${id}/reactivate`, request);
  }
}
