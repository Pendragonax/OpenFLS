import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {BackupStatusService} from './backup-status.service';
import {environment} from '../../../environments/environment';

describe('BackupStatusService', () => {
  let service: BackupStatusService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BackupStatusService]
    });
    service = TestBed.inject(BackupStatusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('status_should_get_admin_backup_status', () => {
    service.status().subscribe();

    const request = httpMock.expectOne(`${environment.api_url}admin/backup/status`);
    expect(request.request.method).toBe('GET');
    request.flush({lastBackup: null, lastRestoreTest: null, backupOverdue: true, maxAgeHours: 7, overall: 'unknown'});
  });

  it('history_should_default_limit_to_hundred', () => {
    service.history().subscribe();

    const request = httpMock.expectOne(r => r.url === `${environment.api_url}admin/backup/history`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('limit')).toBe('100');
    request.flush([]);
  });

  it('history_should_pass_explicit_limit', () => {
    service.history(25).subscribe();

    const request = httpMock.expectOne(r => r.url === `${environment.api_url}admin/backup/history`);
    expect(request.request.params.get('limit')).toBe('25');
    request.flush([]);
  });
});
