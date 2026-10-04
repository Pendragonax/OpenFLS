import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap, ParamMap, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {BehaviorSubject, of, throwError} from 'rxjs';
import {vi} from 'vitest';

import {ClientDashboardComponent} from './client-dashboard.component';
import {ClientDashboardService} from '../../shared/services/client-dashboard.service';
import {HelperService} from '../../shared/services/helper.service';
import {ClientDashboardDto} from '../../shared/dtos/client-dashboard-dto.model';
import {ClientTaskDto} from '../../shared/dtos/client-task-dto.model';
import {AssistancePlanPreviewDto} from '../../shared/dtos/assistance-plan-preview-dto.model';
import {UserService} from '../../shared/services/user.service';
import {EmployeeDto} from '../../shared/dtos/employee-dto.model';

function dashboard(overrides: Partial<ClientDashboardDto> = {}): ClientDashboardDto {
  return {
    clientId: 3,
    firstName: 'Max',
    lastName: 'Mustermann',
    archived: false,
    institutionId: 5,
    institutionName: 'Inst',
    favorite: false,
    canModifyClient: false,
    canWriteEntries: false,
    assistancePlanAccess: 'GRANTED',
    currentAssistancePlan: null,
    assistancePlanCount: 0,
    servicesAccess: 'GRANTED',
    latestServices: [],
    tasks: [],
    openTaskCount: 0,
    ...overrides
  };
}

describe('ClientDashboardComponent', () => {
  let component: ClientDashboardComponent;
  let fixture: ComponentFixture<ClientDashboardComponent>;
  let clientDashboardService: {
    getDashboard: ReturnType<typeof vi.fn>;
    addFavorite: ReturnType<typeof vi.fn>;
    deleteFavorite: ReturnType<typeof vi.fn>;
  };
  let helperService: { openSnackBar: ReturnType<typeof vi.fn> };
  let paramMap$: BehaviorSubject<ParamMap>;
  let user$: BehaviorSubject<EmployeeDto>;
  let matDialog: { open: ReturnType<typeof vi.fn> };

  /** Mocks the next MatDialog.open() call and resolves afterClosed() with `confirmed`. */
  function mockConfirmationDialog(confirmed: boolean | undefined) {
    matDialog.open.mockReturnValueOnce({
      componentInstance: {},
      afterClosed: () => of(confirmed)
    });
  }

  beforeEach(async () => {
    paramMap$ = new BehaviorSubject(convertToParamMap({id: '3'}));
    user$ = new BehaviorSubject(Object.assign(new EmployeeDto(), {id: 7}));
    clientDashboardService = {
      getDashboard: vi.fn(() => of(dashboard())),
      addFavorite: vi.fn(() => of(undefined)),
      deleteFavorite: vi.fn(() => of(undefined))
    };
    helperService = {openSnackBar: vi.fn()};
    matDialog = {open: vi.fn()};

    await TestBed.configureTestingModule({
      declarations: [ClientDashboardComponent],
      providers: [
        {provide: ClientDashboardService, useValue: clientDashboardService},
        {provide: HelperService, useValue: helperService},
        {provide: UserService, useValue: {user$}},
        {provide: MatDialog, useValue: matDialog},
        {
          provide: ActivatedRoute,
          useValue: {paramMap: paramMap$}
        },
        {provide: Router, useValue: {navigate: vi.fn(() => Promise.resolve(true))}}
      ]
    })
      .overrideComponent(ClientDashboardComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(ClientDashboardComponent);
    component = fixture.componentInstance;
  });

  it('loads the dashboard of the client in the route', () => {
    fixture.detectChanges();

    expect(clientDashboardService.getDashboard).toHaveBeenCalledWith(3);
    expect(component.dashboard?.clientId).toBe(3);
    expect(component.clientName).toBe('Max Mustermann');
    expect(component.loadFailed).toBe(false);
  });

  it('marks a failed load so the template can offer a retry', () => {
    clientDashboardService.getDashboard.mockReturnValueOnce(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.loadFailed).toBe(true);
    expect(component.isLoading).toBe(false);
  });

  it('adds the client to the favourites when it is not favoured yet', () => {
    fixture.detectChanges();

    component.toggleFavorite();

    expect(clientDashboardService.addFavorite).toHaveBeenCalledWith(3);
    expect(component.dashboard?.favorite).toBe(true);
    expect(helperService.openSnackBar).toHaveBeenCalledWith('Zu den Favoriten hinzugefügt');
  });

  it('removes an existing favourite after the user confirms', () => {
    clientDashboardService.getDashboard.mockReturnValueOnce(of(dashboard({favorite: true})));
    fixture.detectChanges();
    mockConfirmationDialog(true);

    component.toggleFavorite();

    expect(clientDashboardService.deleteFavorite).toHaveBeenCalledWith(3);
    expect(component.dashboard?.favorite).toBe(false);
  });

  it('keeps an existing favourite when the user declines the confirmation', () => {
    clientDashboardService.getDashboard.mockReturnValueOnce(of(dashboard({favorite: true})));
    fixture.detectChanges();
    mockConfirmationDialog(false);

    component.toggleFavorite();

    expect(clientDashboardService.deleteFavorite).not.toHaveBeenCalled();
    expect(component.dashboard?.favorite).toBe(true);
  });

  it('keeps the favourite state when the request fails', () => {
    fixture.detectChanges();
    clientDashboardService.addFavorite.mockReturnValueOnce(throwError(() => new Error('boom')));

    component.toggleFavorite();

    expect(component.dashboard?.favorite).toBe(false);
    expect(helperService.openSnackBar).toHaveBeenCalledWith('Favorit konnte nicht geändert werden');
  });

  it('reloads when the route switches to another client', () => {
    fixture.detectChanges();
    clientDashboardService.getDashboard.mockReturnValueOnce(of(dashboard({clientId: 9, firstName: 'Mia'})));

    paramMap$.next(convertToParamMap({id: '9'}));

    expect(clientDashboardService.getDashboard).toHaveBeenLastCalledWith(9);
    expect(component.dashboard?.clientId).toBe(9);
  });

  it('recounts the open tasks when the task card reports a change', () => {
    fixture.detectChanges();

    const tasks: ClientTaskDto[] = [
      Object.assign(new ClientTaskDto(), {id: 1, done: false}),
      Object.assign(new ClientTaskDto(), {id: 2, done: true})
    ];
    component.onTasksChanged(tasks);

    expect(component.dashboard?.openTaskCount).toBe(1);
    expect(component.dashboard?.tasks.length).toBe(2);
  });

  it('preselects the client and the period of the relevant plan for "Alle Einträge"', () => {
    const plan = Object.assign(new AssistancePlanPreviewDto(), {
      id: 1,
      start: '2026-01-01',
      end: '2026-12-31'
    });
    clientDashboardService.getDashboard.mockReturnValueOnce(of(dashboard({currentAssistancePlan: plan})));

    fixture.detectChanges();

    expect(component.allEntriesRouterLink).toEqual(['/services/all', '2026-01-01', '2026-12-31', 0, 0, 3]);
  });

  it('falls back to an unfiltered date range for "Alle Einträge" without a plan', () => {
    fixture.detectChanges();

    expect(component.allEntriesRouterLink).toEqual(['/services/all', 0, 0, 0, 0, 3]);
  });

  it('picks up the signed in employee id for the "own entry/task" highlight', () => {
    fixture.detectChanges();

    expect(component.currentEmployeeId).toBe(7);
  });
});
