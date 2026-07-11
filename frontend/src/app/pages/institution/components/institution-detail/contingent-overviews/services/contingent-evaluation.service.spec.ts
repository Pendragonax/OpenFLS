import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ContingentEvaluationService} from './contingent-evaluation.service';
import {environment} from '../../../../../../../environments/environment';

describe('ContingentEvaluationService', () => {
  let service: ContingentEvaluationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ContingentEvaluationService]
    });

    service = TestBed.inject(ContingentEvaluationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getOverviewByInstitutionIdAndYear_includeArchivedTrue_should_requestArchivedEmployees', () => {
    service.getOverviewByInstitutionIdAndYear(9, 2026, true).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}contingents/evaluations/institution/9/2026?includeArchivedEmployees=true`);
    expect(request.request.method).toBe('GET');
    request.flush({employees: []});
  });
});
