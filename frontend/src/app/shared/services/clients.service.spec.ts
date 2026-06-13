import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpHeaders} from '@angular/common/http';
import {HttpTestingController} from '@angular/common/http/testing';
import {vi} from 'vitest';
import {ClientsService} from './clients.service';
import {environment} from '../../../environments/environment';
import {ClientArchiveExportFormat} from '../dtos/client-archive-export-format.model';
import {ClientArchiveExportRequest} from '../dtos/client-archive-export-request.model';
import {saveAs} from 'file-saver-es';

vi.mock('file-saver-es', () => ({
  saveAs: vi.fn()
}));

describe('ClientsService', () => {
  let service: ClientsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClientsService]
    });

    service = TestBed.inject(ClientsService);
    httpMock = TestBed.inject(HttpTestingController);

    const initialLoad = httpMock.expectOne(`${environment.api_url}clients`);
    expect(initialLoad.request.method).toBe('GET');
    initialLoad.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
    vi.mocked(saveAs).mockReset();
  });

  it('getArchiveExportStatusById_should_return_export_status', () => {
    let result: any;

    service.getArchiveExportStatusById(12).subscribe(value => result = value);

    const request = httpMock.expectOne(`${environment.api_url}clients/12/archive/export`);
    expect(request.request.method).toBe('GET');
    request.flush({
      ready: true,
      format: 'JSON',
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 4,
      downloadLink: {
        downloadLink: '/clients/12/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    });

    expect(result.downloadLink.downloadLink).toBe('/clients/12/archive/export/token-123');
    expect(result.ready).toBe(true);
    expect(result.format).toBe(ClientArchiveExportFormat.JSON);
  });

  it('requestArchiveExport_should_post_export_request', () => {
    let result: any;
    const requestBody = new ClientArchiveExportRequest();

    service.requestArchiveExport(12, requestBody).subscribe(value => result = value);

    const request = httpMock.expectOne(`${environment.api_url}clients/12/archive/export`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.format).toBe(ClientArchiveExportFormat.JSON);
    request.flush({
      ready: true,
      format: 'JSON',
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 4,
      downloadLink: {
        downloadLink: '/clients/12/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    });

    expect(result.downloadLink.downloadLink).toBe('/clients/12/archive/export/token-123');
    expect(result.requestedByEmployeeId).toBe(4);
  });

  it('requestArchiveExport_should_post_anonymize_flag', () => {
    const requestBody = new ClientArchiveExportRequest();
    requestBody.anonymize = true;

    service.requestArchiveExport(12, requestBody).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}clients/12/archive/export`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.anonymize).toBe(true);
    request.flush({
      ready: true,
      format: 'JSON',
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 4,
      downloadLink: {
        downloadLink: '/clients/12/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    });
  });

  it('downloadArchiveExport_should_save_blob_with_filename_from_header', () => {
    let completed = false;
    const downloadLink = {
      downloadLink: '/clients/12/archive/export/token-123',
      downloadLinkExpiresAt: '2026-06-13T10:35:00',
      downloadedAt: null
    };

    service.downloadArchiveExport(downloadLink).subscribe(() => completed = true);

    const request = httpMock.expectOne(`${environment.api_url}clients/12/archive/export/token-123`);
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');

    const blob = new Blob(['{"client":true}'], {type: 'application/json'});
    request.flush(blob, {
      headers: new HttpHeaders({'content-disposition': 'attachment; filename="client-12-archive-export.json"'})
    });

    expect(completed).toBe(true);
    expect(vi.mocked(saveAs)).toHaveBeenCalledWith(blob, 'client-12-archive-export.json');
  });
});
