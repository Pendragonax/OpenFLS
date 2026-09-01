import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {
  ClientTaskAuditLogDto,
  ClientTaskDto,
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

  create(value: CreateClientTaskDto): Observable<ClientTaskDto> {
    return this.http.post<ClientTaskDto>(this.url, value);
  }

  update(value: UpdateClientTaskDto): Observable<ClientTaskDto> {
    return this.http.put<ClientTaskDto>(`${this.url}/${value.id}`, value);
  }

  complete(id: number, value: CompleteClientTaskDto): Observable<ClientTaskDto> {
    return this.http.post<ClientTaskDto>(`${this.url}/${id}/complete`, value);
  }

  reopen(id: number, comment: string): Observable<ClientTaskDto> {
    return this.http.post<ClientTaskDto>(`${this.url}/${id}/reopen`, {comment, completedOn: new Date().toISOString().substring(0, 10)});
  }

  delete(id: number): Observable<ClientTaskDto> {
    return this.http.delete<ClientTaskDto>(`${this.url}/${id}`);
  }

  getHistory(id: number): Observable<ClientTaskAuditLogDto[]> {
    return this.http.get<ClientTaskAuditLogDto[]>(`${this.url}/${id}/history`);
  }
}
