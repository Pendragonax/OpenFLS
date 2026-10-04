import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {
  ClientTaskAuditLogDto,
  ClientTaskDto,
  ClientTaskPageDto,
  CompleteClientTaskDto,
  CreateClientTaskDto,
  UpdateClientTaskDto
} from '../dtos/client-task-dto.model';

/**
 * Client tasks may be read, created and completed by every employee. The backend
 * records who changed what and when.
 */
@Injectable({
  providedIn: 'root'
})
export class ClientTaskService {
  private readonly url = `${environment.api_url}client_tasks`;

  constructor(private http: HttpClient) {
  }

  getByClientId(clientId: number): Observable<ClientTaskDto[]> {
    return this.http.get<ClientTaskDto[]>(`${this.url}/client/${clientId}`);
  }

  getCompletedByClientId(clientId: number, page: number, size: number = 10): Observable<ClientTaskPageDto> {
    return this.http.get<ClientTaskPageDto>(`${this.url}/client/${clientId}/completed?page=${page}&size=${size}`);
  }

  create(value: CreateClientTaskDto): Observable<ClientTaskDto> {
    return this.http.post<ClientTaskDto>(this.url, value);
  }

  update(id: number, value: UpdateClientTaskDto): Observable<ClientTaskDto> {
    return this.http.put<ClientTaskDto>(`${this.url}/${id}/change`, value);
  }

  complete(id: number, value: CompleteClientTaskDto): Observable<ClientTaskDto> {
    return this.http.post<ClientTaskDto>(`${this.url}/${id}/complete`, value);
  }

  delete(id: number): Observable<ClientTaskDto> {
    return this.http.delete<ClientTaskDto>(`${this.url}/${id}`);
  }

  getHistory(id: number): Observable<ClientTaskAuditLogDto[]> {
    return this.http.get<ClientTaskAuditLogDto[]>(`${this.url}/${id}/history`);
  }
}
