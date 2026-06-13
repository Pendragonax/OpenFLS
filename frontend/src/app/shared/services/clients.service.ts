import { Injectable } from '@angular/core';
import {ClientDto} from "../dtos/client-dto.model";
import {Base} from "./base.service";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Observable, map, tap} from "rxjs";
import {environment} from "../../../environments/environment";
import {ClientSoloDto} from "../dtos/client-solo-dto.model";
import {ClientForServiceEditingDto} from "../dtos/client-for-service-editing-dto.model";
import {ClientArchiveHistoryEntryReadDto} from "../dtos/client-archive-history-entry-read-dto.model";
import {ClientArchiveActionRequest} from "../dtos/client-archive-action-request.model";
import {ClientArchiveExportRequest} from "../dtos/client-archive-export-request.model";
import {ClientArchiveExportStatusDto} from "../dtos/client-archive-export-status-dto.model";
import {ClientArchiveExportDownloadLinkDto} from "../dtos/client-archive-export-download-link-dto.model";
import {saveAs} from "file-saver-es";

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

  getArchiveExportStatusById(id: number): Observable<ClientArchiveExportStatusDto> {
    return this.http
      .get<ClientArchiveExportStatusDto>(`${environment.api_url}${this.url}/${id}/archive/export`)
      .pipe(map(value => this.transformToArchiveExportStatus(value)));
  }

  requestArchiveExport(id: number, request: ClientArchiveExportRequest): Observable<ClientArchiveExportStatusDto> {
    return this.http
      .post<ClientArchiveExportStatusDto>(`${environment.api_url}${this.url}/${id}/archive/export`, request)
      .pipe(map(value => this.transformToArchiveExportStatus(value)));
  }

  downloadArchiveExport(downloadLink: ClientArchiveExportDownloadLinkDto): Observable<void> {
    const normalizedDownloadLink = downloadLink.downloadLink.startsWith('/')
      ? downloadLink.downloadLink.substring(1)
      : downloadLink.downloadLink;

    return this.http
      .get(`${environment.api_url}${normalizedDownloadLink}`, {
        responseType: 'blob',
        observe: 'response'
      })
      .pipe(
        tap(response => this.saveExportFile(response)),
        map(() => void 0)
      );
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

  private transformToArchiveExportStatus(data: any): ClientArchiveExportStatusDto {
    return {
      ready: data.ready,
      format: data.format,
      requestedAt: data.requestedAt,
      requestedByEmployeeId: data.requestedByEmployeeId,
      downloadLink: this.transformToArchiveExportDownloadLink(data.downloadLink)
    };
  }

  private transformToArchiveExportDownloadLink(data: any): ClientArchiveExportDownloadLinkDto | null {
    if (data == null) {
      return null;
    }

    return {
      downloadLink: data.downloadLink,
      downloadLinkExpiresAt: data.downloadLinkExpiresAt,
      downloadedAt: data.downloadedAt
    };
  }

  private saveExportFile(response: HttpResponse<Blob>) {
    const fileName = this.resolveExportFileName(response) ?? this.resolveExportFileNameFromLink();
    const blob = response.body ?? new Blob();
    saveAs(blob, fileName);
  }

  private resolveExportFileName(response: HttpResponse<Blob>): string | null {
    const contentDisposition = response.headers.get('content-disposition') ?? '';
    const match = contentDisposition.match(/filename="?([^"]+)"?/i);
    return match?.[1] ?? null;
  }

  private resolveExportFileNameFromLink(): string {
    return 'client-archive-export.json';
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
