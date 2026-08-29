import {of} from 'rxjs';
import {describe, expect, it, vi} from 'vitest';
import {AssistancePlanNewPageComponent} from './assistance-plan-new.component';
import {AssistancePlanExistingDto} from '../../../shared/dtos/assistance-plan-existing-dto.model';
import {AssistancePlanHourMode} from '../../../shared/dtos/assistance-plan-hour-mode.model';
import {HourCorridorDto} from '../../../shared/dtos/hour-corridor-dto.model';

describe('AssistancePlanNewPageComponent', () => {
  function createComponent(existingPlans: AssistancePlanExistingDto[]) {
    const assistancePlanService = {
      getExistingByClientId: vi.fn().mockReturnValue(of(existingPlans))
    };
    const hourCorridorService = {
      allValues$: of([])
    };

    const component = new AssistancePlanNewPageComponent(
      {allValues$: of([])} as any,
      {allValues$: of([])} as any,
      assistancePlanService as any,
      {openSnackBar: vi.fn()} as any,
      {back: vi.fn()} as any,
      {snapshot: {paramMap: {get: () => '1'}}} as any,
      {getById: vi.fn().mockReturnValue(of({id: 1, firstName: 'Max', lastName: 'Meier'}))} as any,
      {affiliatedInstitutions$: of([]), isAdmin$: of(false)} as any,
      hourCorridorService as any,
      {formatDate: vi.fn(), formatDateToGerman: vi.fn((d: Date) => d.toISOString().slice(0, 10))} as any
    );

    return {component, assistancePlanService, hourCorridorService};
  }

  it('loads existing plans via dedicated existing endpoint', () => {
    const plans = [
      {id: 2, start: '2026-03-01', end: '2026-04-01', sponsorName: 'B'},
      {id: 1, start: '2026-01-01', end: '2026-02-01', sponsorName: 'A'}
    ] as AssistancePlanExistingDto[];
    const {component, assistancePlanService} = createComponent(plans);

    (component as any).loadExistingPlans(99);

    expect(assistancePlanService.getExistingByClientId).toHaveBeenCalledWith(99);
    expect(component.existingPlans.map(plan => plan.id)).toEqual([1, 2]);
  });

  it('clears plan and goal hours when switching to corridor mode', () => {
    const {component} = createComponent([]);
    component.createValue.hours = [{weeklyMinutes: 120, hourTypeId: 1} as any];
    component.createValue.goals = [{hours: [{weeklyMinutes: 60, hourTypeId: 2}]} as any];

    (component as any).syncHourModeState(AssistancePlanHourMode.CORRIDOR, true);

    expect(component.createValue.hourMode).toBe(AssistancePlanHourMode.CORRIDOR);
    expect(component.createValue.hours).toEqual([]);
    expect(component.createValue.goals.every(goal => goal.hours.length === 0)).toBe(true);
  });

  it('formats corridor minutes as hours and minutes with a unit', () => {
    const {component} = createComponent([]);
    const corridor = new HourCorridorDto({
      title: 'Korridor', weeklyMinutesFrom: 6, weeklyMinutesTill: 481, hourTypeTitle: 'Fachleistung'
    });

    expect(component.getHourCorridorLabel(corridor)).toBe('Korridor · 0:06 h - 8:01 h · Fachleistung');
  });
});
