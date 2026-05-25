import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpErrorResponse} from '@angular/common/http';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';

import {AssistancePlanEvaluationModalComponent} from './assistance-plan-evaluation-modal.component';
import {EvaluationsService} from '../../../../shared/services/evaluations.service';
import {DateService} from '../../../../shared/services/date.service';
import {HelperService} from '../../../../shared/services/helper.service';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';

describe('GoalEvaluationModalComponent', () => {
  let component: AssistancePlanEvaluationModalComponent;
  let fixture: ComponentFixture<AssistancePlanEvaluationModalComponent>;
  let helperService: {openSnackBar: ReturnType<typeof vi.fn>};
  let dialogRef: {close: ReturnType<typeof vi.fn>};
  let evaluationsService: {create: ReturnType<typeof vi.fn>};

  beforeEach(async () => {
    helperService = {openSnackBar: vi.fn()};
    dialogRef = {close: vi.fn()};
    evaluationsService = {
      create: vi.fn().mockReturnValue(throwError(() => new HttpErrorResponse({
        status: 409,
        error: {message: 'Der zugehörige Klient ist archiviert.'}
      }))),
      update: vi.fn(),
      delete: vi.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [AssistancePlanEvaluationModalComponent],
      providers: [
        {provide: EvaluationsService, useValue: evaluationsService},
        {provide: DateService, useValue: {formatDateString: () => '', formatDateToYearMonthDay: () => '2026-01-01'}},
        {provide: HelperService, useValue: helperService},
        {provide: MatDialogRef, useValue: dialogRef},
        {provide: MatDialog, useValue: {open: () => ({componentInstance: {description: ''}, afterClosed: () => of(null)})}}
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanEvaluationModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the backend conflict reason when saving is rejected', () => {
    component.onSaveClick();

    expect(component.operationErrorMessage).toContain('archiviert');
    expect(helperService.openSnackBar).toHaveBeenCalledWith(expect.stringContaining('archiviert'));
    expect(dialogRef.close).not.toHaveBeenCalled();
  });
});
