import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {BehaviorSubject, of} from 'rxjs';
import {vi} from 'vitest';

import {EmployeeDetailComponent} from './employee-detail.component';
import {EmployeeDto} from '../../../../shared/dtos/employee-dto.model';
import {InstitutionDto} from '../../../../shared/dtos/institution-dto.model';
import {EmployeeService} from '../../../../shared/services/employee.service';
import {InstitutionService} from '../../../../shared/services/institution.service';
import {UserService} from '../../../../shared/services/user.service';
import {DtoCombinerService} from '../../../../shared/services/dto-combiner.service';
import {HelperService} from '../../../../shared/services/helper.service';
import {EmployeeArchiveService} from '../../../../shared/services/employee-archive.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {MatDialog} from '@angular/material/dialog';
import {EmployeeArchiveHistoryEntryReadDto} from '../../../../shared/dtos/employee-archive-history-entry-read-dto.model';
import {convertToParamMap} from '@angular/router';

describe('EmployeeDetailComponent', () => {
  let component: EmployeeDetailComponent;
  let fixture: ComponentFixture<EmployeeDetailComponent>;

  const employee = createEmployee(true);
  const history: EmployeeArchiveHistoryEntryReadDto[] = [createHistoryEntry()];

  const employeeService = {
    getById: vi.fn(() => of(employee))
  };

  const institutionService = {
    allValues$: of([Object.assign(new InstitutionDto(), {id: 1, name: 'Bereich A'})])
  };

  const userService = {
    leadingInstitutions$: new BehaviorSubject<number[]>([]),
    user$: of(createUser())
  };

  const dtoCombinerService = {
    combinePermissionsByEmployee: vi.fn(() => [])
  };

  const employeeArchiveService = {
    getArchiveHistoryById: vi.fn(() => of(history)),
    archive: vi.fn(() => of(history[0])),
    reactivate: vi.fn(() => of(history[0]))
  };

  const route = {
    snapshot: {
      paramMap: convertToParamMap({id: '1'})
    }
  } as unknown as ActivatedRoute;

  beforeEach(async () => {
    employeeService.getById.mockClear();
    dtoCombinerService.combinePermissionsByEmployee.mockClear();
    employeeArchiveService.getArchiveHistoryById.mockClear();

    await TestBed.configureTestingModule({
      declarations: [EmployeeDetailComponent],
      providers: [
        {provide: ActivatedRoute, useValue: route},
        {provide: EmployeeService, useValue: employeeService},
        {provide: InstitutionService, useValue: institutionService},
        {provide: UserService, useValue: userService},
        {provide: DtoCombinerService, useValue: dtoCombinerService},
        {provide: EmployeeArchiveService, useValue: employeeArchiveService},
        {provide: HelperService, useValue: {openSnackBar: () => void 0}},
        {provide: NgbModal, useValue: {open: vi.fn(() => ({result: Promise.resolve(false)}))}},
        {provide: MatDialog, useValue: {open: vi.fn(() => ({componentInstance: {}, afterClosed: () => of(false)}))}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loadValues_adminArchivedEmployee_locksGeneralFormAndLoadsArchiveHistory', () => {
    expect(component.showArchiveTab).toBe(true);
    expect(component.archiveHistory).toHaveLength(1);
    expect(component.detailForm.disabled).toBe(true);
    expect(component.isArchived).toBe(true);
  });
});

function createEmployee(archived: boolean): EmployeeDto {
  const employee = new EmployeeDto();
  employee.id = 1;
  employee.firstName = 'Anna';
  employee.lastName = 'Beispiel';
  employee.email = 'anna@example.org';
  employee.phonenumber = '0123';
  employee.description = 'Desc';
  employee.archived = archived;
  employee.permissions = [];
  employee.unprofessionals = [];
  employee.inactive = false;
  employee.institutionId = 1;
  employee.access!.role = 1;
  return employee;
}

function createUser(): EmployeeDto {
  const user = new EmployeeDto();
  user.access!.role = 1;
  user.permissions = [];
  return user;
}

function createHistoryEntry(): EmployeeArchiveHistoryEntryReadDto {
  const entry = new EmployeeArchiveHistoryEntryReadDto();
  entry.id = 1;
  entry.actionType = 'ARCHIVE';
  entry.actionDate = '2026-07-04';
  entry.actionTimestamp = '2026-07-04T10:00:00';
  entry.reason = 'Grund';
  entry.remark = 'Bemerkung';
  entry.executingEmployeeId = 1;
  entry.executingEmployeeFirstname = 'Anna';
  entry.executingEmployeeLastname = 'Beispiel';
  return entry;
}
