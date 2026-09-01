import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';

import {ClientTaskCreateModalComponent} from './client-task-create-modal.component';
import {ClientTaskService} from '../../../../../../shared/services/client-task.service';
import {HelperService} from '../../../../../../shared/services/helper.service';
import {ClientTaskDto} from '../../../../../../shared/dtos/client-task-dto.model';
import {Converter} from '../../../../../../shared/services/converter.helper';

describe('ClientTaskCreateModalComponent', () => {
  let component: ClientTaskCreateModalComponent;
  let fixture: ComponentFixture<ClientTaskCreateModalComponent>;
  let clientTaskService: { create: ReturnType<typeof vi.fn> };
  let helperService: { openSnackBar: ReturnType<typeof vi.fn> };
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    clientTaskService = {create: vi.fn(() => of(new ClientTaskDto()))};
    helperService = {openSnackBar: vi.fn()};
    dialogRef = {close: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ClientTaskCreateModalComponent],
      providers: [
        {provide: ClientTaskService, useValue: clientTaskService},
        {provide: HelperService, useValue: helperService},
        {provide: Converter, useValue: {formatDate: (value: Date) => `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`}},
        {provide: MatDialogRef, useValue: dialogRef}
      ]
    })
      .overrideComponent(ClientTaskCreateModalComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(ClientTaskCreateModalComponent);
    component = fixture.componentInstance;
    component.clientId = 3;
  });

  it('does not submit without a title', () => {
    component.form.controls.title.setValue('');

    component.submit();

    expect(clientTaskService.create).not.toHaveBeenCalled();
  });

  it('creates the task for the given client and closes with the result', () => {
    const created = Object.assign(new ClientTaskDto(), {id: 9, title: 'Neu'});
    clientTaskService.create.mockReturnValueOnce(of(created));
    component.form.setValue({title: 'Neu', description: 'Details', dueDate: new Date(2026, 3, 1)});

    component.submit();

    expect(clientTaskService.create).toHaveBeenCalledWith({
      clientId: 3,
      title: 'Neu',
      description: 'Details',
      dueDate: '2026-04-01'
    });
    expect(dialogRef.close).toHaveBeenCalledWith(created);
  });

  it('reports a failed creation without closing', () => {
    clientTaskService.create.mockReturnValueOnce(throwError(() => new Error('boom')));
    component.form.setValue({title: 'Neu', description: '', dueDate: new Date(2026, 3, 1)});

    component.submit();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.isSubmitting).toBe(false);
    expect(helperService.openSnackBar).toHaveBeenCalledWith('Aufgabe konnte nicht angelegt werden');
  });

  it('closes with null on cancel', () => {
    component.cancel();

    expect(dialogRef.close).toHaveBeenCalledWith(null);
  });
});
