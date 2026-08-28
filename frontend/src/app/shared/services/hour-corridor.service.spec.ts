import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {HourCorridorService} from './hour-corridor.service';
import {environment} from '../../../environments/environment';
import {HourCorridorDto} from '../dtos/hour-corridor-dto.model';

describe('HourCorridorService', () => {
  let service: HourCorridorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HourCorridorService]
    });

    service = TestBed.inject(HourCorridorService);
    httpMock = TestBed.inject(HttpTestingController);

    const initialLoad = httpMock.expectOne(`${environment.api_url}hour_corridors`);
    expect(initialLoad.request.method).toBe('GET');
    initialLoad.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('create_should_post_hour_corridor_payload', () => {
    const payload = new HourCorridorDto({title: 'Morgens', weeklyMinutesFrom: 300, weeklyMinutesTill: 600, hourTypeId: 7});
    service.create(payload).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}hour_corridors`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.title).toBe('Morgens');
    request.flush(payload);

    const reload = httpMock.expectOne(`${environment.api_url}hour_corridors`);
    expect(reload.request.method).toBe('GET');
    reload.flush([]);
  });

  it('update_should_put_hour_corridor_payload', () => {
    const payload = new HourCorridorDto({id: 3, title: 'Spät', weeklyMinutesFrom: 420, weeklyMinutesTill: 540, hourTypeId: 8});
    service.update(payload.id, payload).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}hour_corridors/3`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.weeklyMinutesTill).toBe(540);
    request.flush(payload);

    const reload = httpMock.expectOne(`${environment.api_url}hour_corridors`);
    expect(reload.request.method).toBe('GET');
    reload.flush([]);
  });

  it('delete_should_call_hour_corridor_endpoint', () => {
    service.delete(9).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}hour_corridors/9`);
    expect(request.request.method).toBe('DELETE');
    request.flush({});

    const reload = httpMock.expectOne(`${environment.api_url}hour_corridors`);
    expect(reload.request.method).toBe('GET');
    reload.flush([]);
  });

  it('getAuditHistory_should_request_history_endpoint', () => {
    service.getAuditHistory(12).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}hour_corridors/12/history`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
