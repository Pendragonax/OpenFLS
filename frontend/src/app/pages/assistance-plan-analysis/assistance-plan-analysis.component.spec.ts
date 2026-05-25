import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Subject, of} from 'rxjs';
import {AssistancePlanAnalysisComponent} from './assistance-plan-analysis.component';
import {AssistancePlanService} from '../../shared/services/assistance-plan.service';
import {ClientsService} from '../../shared/services/clients.service';
import {DateService} from '../../shared/services/date.service';
import {EvaluationsService} from '../../shared/services/evaluations.service';
import {GoalTimeEvaluationService} from '../../shared/services/goal-time-evaluation.service';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {AssistancePlan} from '../../shared/projections/assistance-plan.projection';
import {vi} from 'vitest';

describe('GoalEvaluationComponent', () => {
  let component: AssistancePlanAnalysisComponent;
  let fixture: ComponentFixture<AssistancePlanAnalysisComponent>;
  let params$: Subject<any>;

  beforeEach(async () => {
    params$ = new Subject();

    await TestBed.configureTestingModule({
      declarations: [AssistancePlanAnalysisComponent],
      providers: [
        {provide: ActivatedRoute, useValue: {params: params$}},
        {provide: AssistancePlanService, useValue: {getProjectionById: () => of(new AssistancePlan())}},
        {provide: ClientsService, useValue: {allValues$: of([{id: 1, archived: false}])}},
        {provide: DateService, useValue: {getMonths: () => []}},
        {provide: EvaluationsService, useValue: {getByAssistancePlanIdAndYear: () => of({values: []})}},
        {provide: GoalTimeEvaluationService, useValue: {getByYear: () => of({goalTimeEvaluations: [], approvedHours: [], summedApprovedHours: [], executedHours: [], summedExecutedHours: [], approvedHoursLeft: [], summedApprovedHoursLeft: []})}},
        {provide: MatDialog, useValue: {open: () => ({componentInstance: {evaluation$: {next: () => {}}, goalId$: {next: () => {}}, date$: {next: () => {}}, readOnly$: {next: () => {}}}, afterClosed: () => of(null)})}},
        {provide: Location, useValue: {go: vi.fn()}},
      ],
    })
      .overrideComponent(AssistancePlanAnalysisComponent, {set: {template: ''}})
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('opens archived evaluation cells in readonly mode', () => {
    component.clientArchived = true;
    const rows = component.getTableButtonsAsRow({
      goalId: 10,
      title: 'Testziel',
      months: [{assistancePlanActive: true, evaluation: null}]
    } as any, 2026);

    expect(rows[1].enabled).toBe(true);
    expect(rows[1].payload.readOnly).toBe(true);
  });

  it('passes readonly state into the evaluation modal', () => {
    const readOnlyNext = vi.fn();
    const dialog = {
      componentInstance: {
        evaluation$: {next: vi.fn()},
        goalId$: {next: vi.fn()},
        date$: {next: vi.fn()},
        readOnly$: {next: readOnlyNext}
      },
      afterClosed: () => of(null)
    } as any;

    const matDialog = TestBed.inject(MatDialog) as unknown as {open: ReturnType<typeof vi.fn>};
    matDialog.open = vi.fn().mockReturnValue(dialog);

    component.openEvaluationModal({
      goalId: 10,
      date: new Date('2026-05-23T00:00:00'),
      evaluation: {} as any,
      readOnly: true
    });

    expect(readOnlyNext).toHaveBeenCalledWith(true);
  });

  it('unsubscribe_afterDestroy_doesNotUpdateState', () => {
    // Given
    fixture.detectChanges();
    params$.next({tab: '1', id: '5'});
    expect(component.tabIndex).toBe(1);
    expect(component.assistancePlanId).toBe(5);

    // When
    fixture.destroy();
    params$.next({tab: '2', id: '6'});

    // Then
    expect(component.tabIndex).toBe(1);
    expect(component.assistancePlanId).toBe(5);
  });
});
