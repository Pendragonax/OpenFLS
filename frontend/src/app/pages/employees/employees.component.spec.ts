import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BehaviorSubject, of} from 'rxjs';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {vi} from 'vitest';

import {EmployeesComponent} from './employees.component';
import {EmployeeDto} from '../../shared/dtos/employee-dto.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {HelperService} from '../../shared/services/helper.service';
import {EmployeeService} from '../../shared/services/employee.service';
import {UserService} from '../../shared/services/user.service';
import {ServiceService} from '../../shared/services/service.service';
import {Comparer} from '../../shared/services/comparer.helper';
import {InstitutionService} from '../../shared/services/institution.service';

describe('EmployeesComponent', () => {
  let component: EmployeesComponent;
  let fixture: ComponentFixture<EmployeesComponent>;

  const activeEmployee = createEmployee(1, 'Active', false);
  const archivedEmployee = createEmployee(2, 'Archived', true);

  const employeeService = {
    getAll: vi.fn((includeArchived: boolean) => of(includeArchived ? [activeEmployee, archivedEmployee] : [activeEmployee]))
  };

  const userService = {
    leadingInstitutions$: new BehaviorSubject<number[]>([]),
    user$: of({
      access: {role: 1},
      permissions: []
    } as EmployeeDto)
  };

  const institutionService = {
    getAll: vi.fn(() => of([]))
  };

  beforeEach(async () => {
    employeeService.getAll.mockClear();
    institutionService.getAll.mockClear();

    await TestBed.configureTestingModule({
      declarations: [EmployeesComponent],
      providers: [
        {provide: NgbModal, useValue: {}},
        {provide: HelperService, useValue: {openSnackBar: () => void 0}},
        {provide: EmployeeService, useValue: employeeService},
        {provide: UserService, useValue: userService},
        {provide: ServiceService, useValue: {}},
        {provide: Comparer, useValue: {compare: () => 0}},
        {provide: InstitutionService, useValue: institutionService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('onArchivedVisibilityChanged_adminLoadsArchivedEmployees', () => {
    expect(component.values.map(value => value.dto.id)).toEqual([1]);

    component.onArchivedVisibilityChanged(true);

    expect(employeeService.getAll).toHaveBeenCalledWith(true);
    expect(component.values.map(value => value.dto.id)).toEqual([1, 2]);
  });
});

function createEmployee(id: number, lastName: string, archived: boolean): EmployeeDto {
  const employee = new EmployeeDto();
  employee.id = id;
  employee.firstName = 'Test';
  employee.lastName = lastName;
  employee.archived = archived;
  employee.permissions = [];
  employee.access!.role = 3;
  return employee;
}
