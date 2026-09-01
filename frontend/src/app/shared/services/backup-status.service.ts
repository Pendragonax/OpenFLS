import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {BackupHistoryEntryDto, BackupStatusDto} from '../dtos/backup-status-dto.model';

/**
 * Read-only client for the backup operations view (`/admin/backup`). It only
 * reads status metadata; there are no trigger or download endpoints.
 */
@Injectable({providedIn: 'root'})
export class BackupStatusService {
  private readonly baseUrl = `${environment.api_url}admin/backup`;

  constructor(private http: HttpClient) {}

  status(): Observable<BackupStatusDto> {
    return this.http.get<BackupStatusDto>(`${this.baseUrl}/status`);
  }

  history(limit = 100): Observable<BackupHistoryEntryDto[]> {
    return this.http.get<BackupHistoryEntryDto[]>(`${this.baseUrl}/history`, {
      params: new HttpParams().set('limit', String(limit))
    });
  }
}
