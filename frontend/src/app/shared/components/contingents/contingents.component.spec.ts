import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BehaviorSubject, of, ReplaySubject} from 'rxjs';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {vi} from 'vitest';

import {ContingentsComponent} from './contingents.component';
import {ContingentsService} from '../../services/contingents.service';
import {InstitutionService} from '../../services/institution.service';
import {EmployeeService} from '../../services/employee.service';
import {UserService} from '../../services/user.service';
import {Comparer} from '../../services/comparer.helper';
import {Converter} from '../../services/converter.helper';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {HelperService} from '../../services/helper.service';
import {EmployeeDto} from '../../dtos/employee-dto.model';
import {InstitutionDto} from '../../dtos/institution-dto.model';
import {InstitutionViewModel} from '../../models/institution-view.model';

describe('ContingentsComponent', () => {
  let component: ContingentsComponent;
  let fixture: ComponentFixture<ContingentsComponent>;

  const contingentsService = {
    getCombinationByEmployeeId: vi.fn(() => of([])),
    getCombinationByInstitutionId: vi.fn(() => of([]))
  };

  const institutionService = {
    allValues$: of([Object.assign(new InstitutionDto(), {id: 9, name: 'Bereich A'})]),
    getById: vi.fn(() => of(Object.assign(new InstitutionDto(), {id: 9, name: 'Bereich A'})))
  };

  const employeeService = {
    allValues$: of([]),
    getAll: vi.fn(() => of([]))
  };

  const userService = {
    leadingInstitutions$: new BehaviorSubject<number[]>([]),
    isAdmin$: of(true)
  };

  beforeEach(async () => {
    contingentsService.getCombinationByEmployeeId.mockClear();
    contingentsService.getCombinationByInstitutionId.mockClear();
    employeeService.getAll.mockClear();

    await TestBed.configureTestingModule({
      declarations: [ContingentsComponent],
      providers: [
        {provide: ContingentsService, useValue: contingentsService},
        {provide: InstitutionService, useValue: institutionService},
        {provide: EmployeeService, useValue: employeeService},
        {provide: UserService, useValue: userService},
        {provide: Comparer, useValue: {compare: () => 0}},
        {provide: Converter, useValue: {formatDate: () => '2026-07-04', getLocalDateString: () => '04.07.2026'}},
        {provide: NgbModal, useValue: {open: vi.fn(() => ({result: Promise.resolve(false)}))}},
        {provide: HelperService, useValue: {openSnackBar: () => void 0}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ContingentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loadContingents_showArchivedEmployeesTrue_requestsArchivedEmployees', () => {
    const institutionView = new InstitutionViewModel();
    institutionView.dto.id = 9;
    institutionView.editable = true;

    component.institutionView = institutionView;
    component.showArchivedEmployees = true;
    component.loadContingents();

    expect(contingentsService.getCombinationByInstitutionId).toHaveBeenCalledWith(9, true);
  });

  it('loadContingents_employeeView_showArchivedEmployeesTrue_requestsArchivedEmployees', () => {
    const employeeView = {dto: Object.assign(new EmployeeDto(), {id: 7}), editable: true} as any;

    component.employeeView = employeeView;
    component.showArchivedEmployees = true;
    component.loadContingents();

    expect(contingentsService.getCombinationByEmployeeId).toHaveBeenCalledWith(7, true);
  });

  it('onArchivedVisibilityChanged_emitsSelectionChange', () => {
    const emitted: boolean[] = [];
    component.showArchivedEmployeesChange.subscribe(value => emitted.push(value));

    component.onArchivedVisibilityChanged(true);

    expect(emitted).toEqual([true]);
  });

  it('ngOnChanges_showArchivedEmployeesChangesOnEmployeeView_reloadsContingents', () => {
    const employeeView = {dto: Object.assign(new EmployeeDto(), {id: 7}), editable: true} as any;
    component.employeeView = employeeView;
    component.showArchivedEmployees = true;

    component.ngOnChanges({
      showArchivedEmployees: {
        previousValue: false,
        currentValue: true,
        firstChange: false,
        isFirstChange: () => false
      } as any
    });

    expect(contingentsService.getCombinationByEmployeeId).toHaveBeenCalledWith(7, true);
  });

  it('isArchivedEmployeeContingent_returnsTrueForArchivedEmployees', () => {
    const archivedEmployee = Object.assign(new EmployeeDto(), {archived: true});

    expect(component.isArchivedEmployeeContingent([archivedEmployee, new InstitutionDto(), new InstitutionDto() as any, false] as any)).toBe(true);
  });
});
