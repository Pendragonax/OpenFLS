import '@testbed';
import {TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {EmployeeService} from './employee.service';
import {environment} from '../../../environments/environment';
import {EmployeeDto} from '../dtos/employee-dto.model';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EmployeeService]
    });

    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);

    const initialLoad = httpMock.expectOne(`${environment.api_url}employees?includeArchived=false`);
    expect(initialLoad.request.method).toBe('GET');
    initialLoad.flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getAll_includeArchivedTrue_should_requestArchivedEmployees', () => {
    service.getAll(true).subscribe();

    const request = httpMock.expectOne(`${environment.api_url}employees?includeArchived=true`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('getActiveEmployeeSelections_should_filterInactiveAndArchivedEmployees', () => {
    let result: any[] = [];
    service.getActiveEmployeeSelections().subscribe(values => result = values);

    const request = httpMock.expectOne(`${environment.api_url}employees?includeArchived=false`);
    expect(request.request.method).toBe('GET');
    request.flush([
      employee(1, 'Active', 'Employee', false, false),
      employee(2, 'Archived', 'Employee', true, false),
      employee(3, 'Inactive', 'Employee', false, true)
    ]);

    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(1);
    expect(result[0].firstname).toBe('Active');
  });
});

function employee(id: number, firstName: string, lastName: string, archived: boolean, inactive: boolean): EmployeeDto {
  const value = new EmployeeDto();
  value.id = id;
  value.firstName = firstName;
  value.lastName = lastName;
  value.archived = archived;
  value.inactive = inactive;
  return value;
}
