import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {EmployeeArchiveService} from './employee-archive.service';
import {environment} from '../../../environments/environment';
import {EmployeeArchiveActionRequest} from '../dtos/employee-archive-action-request.model';

describe('EmployeeArchiveService', () => {
  let service: EmployeeArchiveService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EmployeeArchiveService]
    });

    service = TestBed.inject(EmployeeArchiveService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getArchiveHistoryById_should_request_archive_history', () => {
    service.getArchiveHistoryById(12).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}employees/12/archive/history`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('archive_should_post_archive_request', () => {
    const requestBody = new EmployeeArchiveActionRequest();

    service.archive(12, requestBody).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}employees/12/archive`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.reason).toBe('');
    request.flush({
      id: 1,
      actionType: 'ARCHIVE',
      actionDate: '2026-07-04',
      actionTimestamp: '2026-07-04T10:00:00',
      reason: '',
      remark: '',
      executingEmployeeId: 1,
      executingEmployeeFirstname: 'Test',
      executingEmployeeLastname: 'User'
    });
  });

  it('reactivate_should_post_reactivate_request', () => {
    const requestBody = new EmployeeArchiveActionRequest();

    service.reactivate(12, requestBody).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}employees/12/reactivate`);
    expect(request.request.method).toBe('POST');
    request.flush({
      id: 2,
      actionType: 'REACTIVATE',
      actionDate: '2026-07-04',
      actionTimestamp: '2026-07-04T10:00:00',
      reason: '',
      remark: '',
      executingEmployeeId: 1,
      executingEmployeeFirstname: 'Test',
      executingEmployeeLastname: 'User'
    });
  });
});
