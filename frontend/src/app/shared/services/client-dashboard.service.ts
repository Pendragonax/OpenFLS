import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {ClientDashboardDto, ClientFavoriteDto} from '../dtos/client-dashboard-dto.model';

@Injectable({
  providedIn: 'root'
})
export class ClientDashboardService {
  private readonly url = `${environment.api_url}client_dashboards`;

  constructor(private http: HttpClient) {
  }

  getDashboard(clientId: number): Observable<ClientDashboardDto> {
    return this.http.get<ClientDashboardDto>(`${this.url}/client/${clientId}`);
  }

  getFavorites(): Observable<ClientFavoriteDto[]> {
    return this.http.get<ClientFavoriteDto[]>(`${this.url}/favorites`);
  }

  addFavorite(clientId: number): Observable<void> {
    return this.http.post<void>(`${this.url}/favorites/client/${clientId}`, null);
  }

  deleteFavorite(clientId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/favorites/client/${clientId}`);
  }
}
