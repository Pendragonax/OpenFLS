import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';

import {ClientTaskDetailModalComponent} from './client-task-detail-modal.component';
import {ClientTaskService} from '../../../../../../shared/services/client-task.service';
import {HelperService} from '../../../../../../shared/services/helper.service';
import {Converter} from '../../../../../../shared/services/converter.helper';
import {ClientTaskDto} from '../../../../../../shared/dtos/client-task-dto.model';

function task(overrides: Partial<ClientTaskDto> = {}): ClientTaskDto {
  return Object.assign(new ClientTaskDto(), {
    id: 1,
    clientId: 3,
    title: 'Bericht',
    dueDate: '2026-03-20',
    createdByName: 'Anna Autorin',
    done: false,
    overdue: false,
    ...overrides
  });
}

describe('ClientTaskDetailModalComponent', () => {
  let component: ClientTaskDetailModalComponent;
  let fixture: ComponentFixture<ClientTaskDetailModalComponent>;
  let clientTaskService: { complete: ReturnType<typeof vi.fn>; reopen: ReturnType<typeof vi.fn> };
  let helperService: { openSnackBar: ReturnType<typeof vi.fn> };
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    clientTaskService = {
      complete: vi.fn(() => of(task({done: true}))),
      reopen: vi.fn(() => of(task()))
    };
    helperService = {openSnackBar: vi.fn()};
    dialogRef = {close: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ClientTaskDetailModalComponent],
      providers: [
        {provide: ClientTaskService, useValue: clientTaskService},
        {provide: HelperService, useValue: helperService},
        {provide: Converter, useValue: {
          getLocalDateString: (value: string) => value,
          formatDate: (value: Date) => `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`
        }},
        {provide: MatDialogRef, useValue: dialogRef}
      ]
    })
      .overrideComponent(ClientTaskDetailModalComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(ClientTaskDetailModalComponent);
    component = fixture.componentInstance;
    component.task = task();
  });

  it('completes the open task with the entered comment and date and closes with the result', () => {
    const completed = task({done: true, completionComment: 'erledigt'});
    clientTaskService.complete.mockReturnValueOnce(of(completed));
    component.completeForm.setValue({comment: 'erledigt', completedOn: new Date(2026, 2, 10)});

    component.complete();

    expect(clientTaskService.complete).toHaveBeenCalledWith(1, {
      comment: 'erledigt',
      completedOn: '2026-03-10'
    });
    expect(dialogRef.close).toHaveBeenCalledWith(completed);
  });

  it('reports a failed completion without closing', () => {
    clientTaskService.complete.mockReturnValueOnce(throwError(() => new Error('boom')));
    component.completeForm.setValue({comment: '', completedOn: new Date(2026, 2, 10)});

    component.complete();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.isSubmitting).toBe(false);
    expect(helperService.openSnackBar).toHaveBeenCalledWith('Aufgabe konnte nicht abgehakt werden');
  });

  it('reopens a done task and closes with the result', () => {
    component.task = task({done: true});
    const reopened = task({done: false});
    clientTaskService.reopen.mockReturnValueOnce(of(reopened));

    component.reopen();

    expect(clientTaskService.reopen).toHaveBeenCalledWith(1, '');
    expect(dialogRef.close).toHaveBeenCalledWith(reopened);
  });

  it('reports a failed reopen without closing', () => {
    component.task = task({done: true});
    clientTaskService.reopen.mockReturnValueOnce(throwError(() => new Error('boom')));

    component.reopen();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(helperService.openSnackBar).toHaveBeenCalledWith('Aufgabe konnte nicht wieder geöffnet werden');
  });

  it('closes with null when dismissed without acting', () => {
    component.close();

    expect(dialogRef.close).toHaveBeenCalledWith(null);
  });
});
