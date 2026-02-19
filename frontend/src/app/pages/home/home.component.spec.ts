import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, ParamMap, Router } from '@angular/router';
import { BehaviorSubject, firstValueFrom, of } from 'rxjs';
import { vi } from 'vitest';

import { HomeComponent } from './home.component';
import { UserService } from '../../shared/services/user.service';
import { InstitutionService } from '../../shared/services/institution.service';
import { DtoCombinerService } from '../../shared/services/dto-combiner.service';
import { HelperService } from '../../shared/services/helper.service';
import { EmployeeDto } from '../../shared/dtos/employee-dto.model';
import { EmployeeAccessDto } from '../../shared/dtos/employee-access-dto.model';
import { InstitutionDto } from '../../shared/dtos/institution-dto.model';
import { PermissionDto } from '../../shared/dtos/permission-dto.model';
import { PermissionRow } from './models/permission-row.model';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let user$: BehaviorSubject<EmployeeDto>;
  let institutions$: BehaviorSubject<InstitutionDto[]>;
  let queryParamMap$: BehaviorSubject<ParamMap>;
  let userService: { user$: BehaviorSubject<EmployeeDto>; loadUser: ReturnType<typeof vi.fn>; changePassword: ReturnType<typeof vi.fn> };
  let dtoCombinerService: { combinePermissionsByEmployee: ReturnType<typeof vi.fn> };
  let helperService: { openSnackBar: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let routeSnapshot: { queryParamMap: ParamMap };

  beforeEach(async () => {
    const employee = new EmployeeDto();
    employee.firstName = 'Jane';
    employee.lastName = 'Doe';
    const access = new EmployeeAccessDto();
    access.username = 'jdoe';
    access.role = 1;
    employee.access = access;

    user$ = new BehaviorSubject(employee);
    institutions$ = new BehaviorSubject([new InstitutionDto({ id: 10, name: 'Inst' })]);

    const permission = new PermissionDto();
    permission.changeInstitution = true;
    const permissionRow: PermissionRow = [institutions$.value[0], employee, permission];

    userService = {
      user$,
      loadUser: vi.fn(),
      changePassword: vi.fn(() => of({}))
    };

    dtoCombinerService = {
      combinePermissionsByEmployee: vi.fn(() => [permissionRow])
    };

    helperService = {
      openSnackBar: vi.fn()
    };

    queryParamMap$ = new BehaviorSubject(convertToParamMap({}));
    routeSnapshot = { queryParamMap: convertToParamMap({}) };

    router = {
      navigate: vi.fn(() => Promise.resolve(true))
    };

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ HomeComponent ],
      providers: [
        { provide: UserService, useValue: userService },
        { provide: InstitutionService, useValue: { allValues$: institutions$ } },
        { provide: DtoCombinerService, useValue: dtoCombinerService },
        { provide: HelperService, useValue: helperService },
        { provide: ActivatedRoute, useValue: { queryParamMap: queryParamMap$, snapshot: routeSnapshot } },
        { provide: Router, useValue: router },
      ],
    })
    .overrideComponent(HomeComponent, { set: { template: '' } })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('homeVm$ emits view model and updates currentEmployee$', async () => {
    const viewModel = await firstValueFrom(component.homeVm$);
    const currentEmployee = await firstValueFrom(component.currentEmployee$);

    expect(viewModel.employee.firstName).toBe('Jane');
    expect(viewModel.username).toBe('jdoe');
    expect(viewModel.role).toBe('Administrator');
    expect(viewModel.permissions.length).toBe(1);
    expect(currentEmployee).toBe(viewModel.employee);
    expect(dtoCombinerService.combinePermissionsByEmployee).toHaveBeenCalled();
  });

  it('refreshUser triggers user reload', () => {
    component.refreshUser();
    expect(userService.loadUser).toHaveBeenCalled();
  });

  it('resetPasswordForm clears fields', () => {
    component.passwordForm.setValue({
      oldPassword: 'old',
      password1: 'Aa1!aaaaa',
      password2: 'Aa1!aaaaa'
    });

    component.resetPasswordForm();

    expect(component.passwordForm.value).toEqual({
      oldPassword: '',
      password1: '',
      password2: ''
    });
  });

  it('updatePassword calls service on valid form', () => {
    component.passwordForm.setValue({
      oldPassword: 'old',
      password1: 'Aa1!aaaaa',
      password2: 'Aa1!aaaaa'
    });
    component.passwordForm.updateValueAndValidity();

    expect(component.passwordForm.valid).toBe(true);

    component.updatePassword();

    expect(userService.changePassword).toHaveBeenCalled();
    expect(helperService.openSnackBar).toHaveBeenCalledWith('Passwort erfolgreich geändert!');
    expect(component.isSubmitting).toBe(false);
  });

  it('updatePassword does nothing on invalid form', () => {
    component.passwordForm.setValue({
      oldPassword: 'old',
      password1: '',
      password2: ''
    });

    component.updatePassword();

    expect(userService.changePassword).not.toHaveBeenCalled();
  });

  it('initTabSync sets selectedTabIndex from query param', () => {
    queryParamMap$.next(convertToParamMap({ tab: 'hours' }));
    expect(component.selectedTabIndex).toBe(1);
  });

  it('onTabIndexChange writes tab to URL', () => {
    routeSnapshot.queryParamMap = convertToParamMap({});

    component.onTabIndexChange(2);

    expect(router.navigate).toHaveBeenCalledWith([], {
      relativeTo: expect.anything(),
      queryParams: { tab: 'general' },
      queryParamsHandling: 'merge'
    });
  });

  it('onTabIndexChange does not navigate when already on tab', () => {
    routeSnapshot.queryParamMap = convertToParamMap({ tab: 'favorites' });

    component.onTabIndexChange(0);

    expect(router.navigate).not.toHaveBeenCalled();
  });
});
