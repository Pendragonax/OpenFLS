import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';

import {ClientDashboardTasksCardComponent} from './client-dashboard-tasks-card.component';
import {ClientTaskCreateModalComponent} from './modals/client-task-create-modal/client-task-create-modal.component';
import {ClientTaskDetailModalComponent} from './modals/client-task-detail-modal/client-task-detail-modal.component';
import {ClientTaskService} from '../../../../shared/services/client-task.service';
import {HelperService} from '../../../../shared/services/helper.service';
import {Converter} from '../../../../shared/services/converter.helper';
import {ClientTaskDto} from '../../../../shared/dtos/client-task-dto.model';

function task(overrides: Partial<ClientTaskDto> = {}): ClientTaskDto {
  return Object.assign(new ClientTaskDto(), {
    id: 1,
    clientId: 3,
    title: 'Bericht',
    dueDate: '2026-03-20',
    createdAt: '2026-03-01T09:00:00',
    createdByName: 'Anna Autorin',
    done: false,
    overdue: false,
    ...overrides
  });
}

describe('ClientDashboardTasksCardComponent', () => {
  let component: ClientDashboardTasksCardComponent;
  let fixture: ComponentFixture<ClientDashboardTasksCardComponent>;
  let clientTaskService: {
    getByClientId: ReturnType<typeof vi.fn>;
    getCompletedByClientId: ReturnType<typeof vi.fn>;
  };
  let helperService: { openSnackBar: ReturnType<typeof vi.fn> };
  let matDialog: { open: ReturnType<typeof vi.fn> };

  /** Mocks the next MatDialog.open() call and returns its (fake) dialogRef. */
  function mockDialogOpen(closedWith: unknown): { componentInstance: any } {
    const dialogRef = {
      componentInstance: {initialize: vi.fn()},
      afterClosed: () => of(closedWith)
    };
    matDialog.open.mockReturnValueOnce(dialogRef);
    return dialogRef;
  }

  beforeEach(async () => {
    clientTaskService = {
      getByClientId: vi.fn(() => of([task()])),
      getCompletedByClientId: vi.fn(() => of({content: [task({id: 2, done: true})], page: 0, size: 10, totalElements: 1, totalPages: 1}))
    };
    helperService = {openSnackBar: vi.fn()};
    matDialog = {open: vi.fn()};

    await TestBed.configureTestingModule({
      declarations: [ClientDashboardTasksCardComponent],
      providers: [
        {provide: ClientTaskService, useValue: clientTaskService},
        {provide: HelperService, useValue: helperService},
        {provide: Converter, useValue: {getLocalDateString: (value: string) => value}},
        {provide: MatDialog, useValue: matDialog}
      ]
    })
      .overrideComponent(ClientDashboardTasksCardComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(ClientDashboardTasksCardComponent);
    component = fixture.componentInstance;
    component.clientId = 3;
    component.tasks = [task(), task({id: 2, title: 'Erledigt', done: true})];
    fixture.detectChanges();
  });

  it('shows only open tasks until the done ones are requested', () => {
    expect(component.visibleTasks.map(value => value.id)).toEqual([1]);

    component.toggleDoneTasks();

    expect(component.visibleTasks.map(value => value.id)).toEqual([2]);
  });

  it('opens the create modal for the client of the dashboard', () => {
    mockDialogOpen(null);

    component.openCreateModal();

    expect(matDialog.open).toHaveBeenCalledWith(ClientTaskCreateModalComponent);
  });

  it('passes the client id into the create modal', () => {
    const dialogRef = mockDialogOpen(null);

    component.openCreateModal();

    expect(dialogRef.componentInstance.clientId).toBe(3);
  });

  it('reloads the tasks once the create modal returns a created task', () => {
    const created = task({id: 5, title: 'Neu'});
    mockDialogOpen(created);
    clientTaskService.getByClientId.mockReturnValueOnce(of([created]));
    const emitted: ClientTaskDto[][] = [];
    component.tasksChanged.subscribe(value => emitted.push(value));

    component.openCreateModal();

    expect(clientTaskService.getByClientId).toHaveBeenCalledWith(3);
    expect(emitted).toEqual([[created]]);
  });

  it('does not reload when the create modal is dismissed without creating', () => {
    mockDialogOpen(null);

    component.openCreateModal();

    expect(clientTaskService.getByClientId).not.toHaveBeenCalled();
  });

  it('opens the detail modal for the clicked task', () => {
    const clicked = task();
    const dialogRef = mockDialogOpen(null);

    component.openDetailModal(clicked);

    expect(matDialog.open).toHaveBeenCalledWith(ClientTaskDetailModalComponent);
    expect(dialogRef.componentInstance.initialize).toHaveBeenCalledWith(clicked);
  });

  it('reloads the tasks once the detail modal reports a change', () => {
    const updated = task({done: true});
    mockDialogOpen(updated);
    clientTaskService.getByClientId.mockReturnValueOnce(of([updated]));
    const emitted: ClientTaskDto[][] = [];
    component.tasksChanged.subscribe(value => emitted.push(value));

    component.openDetailModal(task());

    expect(clientTaskService.getByClientId).toHaveBeenCalledWith(3);
    expect(emitted).toEqual([[updated]]);
  });

  it('does not reload when the detail modal is closed without a change', () => {
    mockDialogOpen(null);

    component.openDetailModal(task());

    expect(clientTaskService.getByClientId).not.toHaveBeenCalled();
  });

  it('reports a failed reload', () => {
    mockDialogOpen(task());
    clientTaskService.getByClientId.mockReturnValueOnce(throwError(() => new Error('boom')));

    component.openDetailModal(task());

    expect(helperService.openSnackBar).toHaveBeenCalledWith('Aufgaben konnten nicht geladen werden');
  });

  it('marks a task created by the current employee as own', () => {
    component.currentEmployeeId = 7;

    expect(component.isOwnTask(task({createdById: 7}))).toBe(true);
    expect(component.isOwnTask(task({createdById: 9}))).toBe(false);
  });

  it('does not mark anything as own before the current employee id is known', () => {
    component.currentEmployeeId = 0;

    expect(component.isOwnTask(task({createdById: 7}))).toBe(false);
  });
});
