import { Injectable } from '@angular/core';
import {ClientDto} from "../dtos/client-dto.model";
import {Base} from "./base.service";
import { HttpClient } from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {ClientSoloDto} from "../dtos/client-solo-dto.model";
import {map} from "rxjs/operators";
import {ClientForServiceEditingDto} from "../dtos/client-for-service-editing-dto.model";
import {ClientArchiveHistoryEntryReadDto} from "../dtos/client-archive-history-entry-read-dto.model";
import {ClientArchiveActionRequest} from "../dtos/client-archive-action-request.model";

@Injectable({
  providedIn: 'root'
})
export class ClientsService extends Base<ClientDto>{
  url = "clients";

  constructor(
    protected override http: HttpClient
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
    });
  }

  getAllClientSoloDTOs(): Observable<ClientSoloDto[]> {
    return this.http
      .get<ClientSoloDto[]>(`${environment.api_url}${this.url}/solo`)
      .pipe(map(data => data.map(item => this.transformToClientSoloDto(item)))
      );
  }

  getByIdForServiceEditing(id: number): Observable<ClientForServiceEditingDto> {
    return this.http
      .get<ClientForServiceEditingDto>(`${environment.api_url}${this.url}/for-service-editing/${id}`);
  }

  getArchiveHistoryById(id: number): Observable<ClientArchiveHistoryEntryReadDto[]> {
    return this.http
      .get<ClientArchiveHistoryEntryReadDto[]>(`${environment.api_url}${this.url}/${id}/archive/history`);
  }

  archive(id: number, request: ClientArchiveActionRequest): Observable<ClientArchiveHistoryEntryReadDto> {
    return this.http
      .post<ClientArchiveHistoryEntryReadDto>(`${environment.api_url}${this.url}/${id}/archive`, request)
      .pipe(map(value => this.transformToArchiveHistoryEntry(value)));
  }

  reactivate(id: number, request: ClientArchiveActionRequest): Observable<ClientArchiveHistoryEntryReadDto> {
    return this.http
      .post<ClientArchiveHistoryEntryReadDto>(`${environment.api_url}${this.url}/${id}/reactivate`, request)
      .pipe(map(value => this.transformToArchiveHistoryEntry(value)));
  }

  private transformToArchiveHistoryEntry(data: any): ClientArchiveHistoryEntryReadDto {
    return {
      id: data.id,
      actionType: data.actionType,
      actionDate: data.actionDate,
      actionTimestamp: data.actionTimestamp,
      reason: data.reason,
      remark: data.remark,
      executingEmployeeId: data.executingEmployeeId,
      executingEmployeeFirstname: data.executingEmployeeFirstname,
      executingEmployeeLastname: data.executingEmployeeLastname
    };
  }

  transformToClientSoloDto(data: any): ClientSoloDto {
    return {
      id: data.id,
      firstName: data.firstName,
      lastName: data.lastName,
      phoneNumber: data.phoneNumber,
      email: data.email
    };
  }
}
