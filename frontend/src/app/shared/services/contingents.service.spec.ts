import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ContingentsService} from './contingents.service';
import {environment} from '../../../environments/environment';
import {EmployeeService} from './employee.service';
import {InstitutionService} from './institution.service';
import {UserService} from './user.service';
import {Converter} from './converter.helper';

describe('ContingentsService', () => {
  let service: ContingentsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ContingentsService,
        {provide: EmployeeService, useValue: {getAll: () => ({pipe: () => ({subscribe: () => void 0})})}},
        {provide: InstitutionService, useValue: {getById: () => ({pipe: () => ({subscribe: () => void 0})}), allValues$: {subscribe: () => void 0}}},
        {provide: UserService, useValue: {leadingInstitutions$: {subscribe: () => void 0}, isAdmin$: {subscribe: () => void 0}}},
        {provide: Converter, useValue: {formatDate: () => '2026-07-04'}}
      ]
    });

    service = TestBed.inject(ContingentsService);
    httpMock = TestBed.inject(HttpTestingController);

    const initialLoad = httpMock.expectOne(`${environment.api_url}contingents`);
    expect(initialLoad.request.method).toBe('GET');
    initialLoad.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getByInstitutionId_includeArchivedTrue_should_requestArchivedEmployees', () => {
    service.getByInstitutionId(9, true).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}contingents/institution/9?includeArchivedEmployees=true`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
