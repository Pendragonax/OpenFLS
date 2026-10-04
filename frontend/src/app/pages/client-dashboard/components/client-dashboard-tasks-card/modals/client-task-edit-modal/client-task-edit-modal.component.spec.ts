import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';
import {ClientTaskEditModalComponent} from './client-task-edit-modal.component';
import {ClientTaskService} from '../../../../../../shared/services/client-task.service';
import {Converter} from '../../../../../../shared/services/converter.helper';
import {HelperService} from '../../../../../../shared/services/helper.service';
import {ClientTaskDto} from '../../../../../../shared/dtos/client-task-dto.model';

describe('ClientTaskEditModalComponent', () => {
  let component: ClientTaskEditModalComponent;
  let fixture: ComponentFixture<ClientTaskEditModalComponent>;
  const taskService = {update: vi.fn()};
  const dialogRef = {close: vi.fn()};
  const helper = {openSnackBar: vi.fn()};

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule], declarations: [ClientTaskEditModalComponent],
      providers: [
        {provide: ClientTaskService, useValue: taskService},
        {provide: MatDialogRef, useValue: dialogRef},
        {provide: HelperService, useValue: helper},
        {provide: Converter, useValue: {formatDate: () => '2026-04-01'}}
      ]
    }).overrideComponent(ClientTaskEditModalComponent, {set: {template: ''}}).compileComponents();
    fixture = TestBed.createComponent(ClientTaskEditModalComponent);
    component = fixture.componentInstance;
    component.initialize(Object.assign(new ClientTaskDto(), {id: 7, title: 'Alt', description: 'Text', dueDate: '2026-03-20'}));
  });

  it('initializes and sends the dedicated change request', () => {
    const updated = Object.assign(new ClientTaskDto(), {id: 7, title: 'Neu'});
    taskService.update.mockReturnValueOnce(of(updated));
    component.form.patchValue({title: 'Neu', description: 'Details', dueDate: new Date(2026, 3, 1)});
    component.submit();
    expect(taskService.update).toHaveBeenCalledWith(7, {title: 'Neu', description: 'Details', dueDate: '2026-04-01'});
    expect(dialogRef.close).toHaveBeenCalledWith(updated);
  });

  it('keeps the dialog open after a failed change', () => {
    taskService.update.mockReturnValueOnce(throwError(() => new Error('boom')));
    component.submit();
    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component.isSubmitting).toBe(false);
  });
});
