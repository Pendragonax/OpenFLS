import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Client} from '@stomp/stompjs';
import {environment} from '../../../environments/environment';
import {LogEntryDto, LogSettingsDto} from '../dtos/log-entry-dto.model';
import {TokenStorageService} from './token.storage.service';

export interface LogQuery { from?: string; to?: string; query?: string; level?: string; logger?: string; thread?: string; all?: boolean; }
export interface LogPage { content: LogEntryDto[]; page: number; size: number; totalElements: number; totalPages: number; hasNext: boolean; }

@Injectable({providedIn: 'root'})
export class LogAdministrationService {
  constructor(private http: HttpClient, private tokenStorage: TokenStorageService) {}

  days(): Observable<string[]> { return this.http.get<string[]>(`${environment.api_url}admin/logs/days`); }
  entries(query: LogQuery): Observable<LogEntryDto[]> { return this.http.get<LogEntryDto[]>(`${environment.api_url}admin/logs`, {params: this.params(query)}); }
  page(query: LogQuery, page = 0, size = 100): Observable<LogPage> {
    return this.http.get<LogPage>(`${environment.api_url}admin/logs/page`, {params: this.params({...query, page, size})});
  }
  settings(): Observable<LogSettingsDto> { return this.http.get<LogSettingsDto>(`${environment.api_url}admin/logs/settings`); }
  setLevel(logger: string, level: string | null): Observable<LogSettingsDto> { return this.http.patch<LogSettingsDto>(`${environment.api_url}admin/logs/settings`, {logger, level}); }
  resetLevel(logger: string): Observable<LogSettingsDto> { return this.http.delete<LogSettingsDto>(`${environment.api_url}admin/logs/settings/${encodeURIComponent(logger)}`); }
  resetLevels(): Observable<LogSettingsDto> { return this.http.delete<LogSettingsDto>(`${environment.api_url}admin/logs/settings`); }
  deleteFrom(from?: string): Observable<void> { return this.http.delete<void>(`${environment.api_url}admin/logs`, {params: from ? {from} : {}}); }
  export(query: LogQuery): Observable<Blob> { return this.http.get(`${environment.api_url}admin/logs/export`, {params: this.params(query), responseType: 'blob'}); }

  liveEntries(): Observable<LogEntryDto> {
    return new Observable<LogEntryDto>(subscriber => {
      const client = new Client({
        webSocketFactory: () => new WebSocket(this.webSocketUrl()),
        connectHeaders: {Authorization: `Bearer ${this.tokenStorage.getTokenString()}`},
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        onConnect: () => client.subscribe('/topic/admin/logs', message => {
          try {
            subscriber.next(JSON.parse(message.body) as LogEntryDto);
          } catch {
            // A malformed event must not terminate the live connection.
          }
        }),
        onStompError: frame => console.error('STOMP-Protokollfehler:', frame.headers['message'] ?? frame.body),
        onWebSocketError: error => console.error('WebSocket-Verbindung fehlgeschlagen:', error)
      });
      client.activate();
      return () => { void client.deactivate(); };
    });
  }

  private webSocketUrl(): string {
    const apiUrl = new URL(environment.api_url, window.location.origin);
    apiUrl.protocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    apiUrl.pathname = `${apiUrl.pathname.replace(/\/$/, '')}/ws`;
    return apiUrl.toString();
  }

  private params(query: LogQuery & {page?: number; size?: number}): HttpParams {
    return Object.entries(query).reduce((params, [key, value]) => value === undefined || value === '' ? params : params.set(key, String(value)), new HttpParams());
  }
}
