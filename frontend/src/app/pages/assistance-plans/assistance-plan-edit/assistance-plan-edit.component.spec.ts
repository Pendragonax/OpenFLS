import {of} from 'rxjs';
import {describe, expect, it} from 'vitest';
import {AssistancePlanDto} from '../../../shared/dtos/assistance-plan-dto.model';
import {AssistancePlanExistingDto} from '../../../shared/dtos/assistance-plan-existing-dto.model';
import {AssistancePlanEditComponent, mapAssistancePlanDtoToUpdateDto} from './assistance-plan-edit.component';

describe('mapAssistancePlanDtoToUpdateDto', () => {
  it('maps assistance plan including nested ids to update dto', () => {
    const plan: AssistancePlanDto = {
      id: 12,
      start: '2026-01-01',
      end: '2026-12-31',
      clientId: 9,
      institutionId: 5,
      sponsorId: 3,
      hours: [
        {id: 101, weeklyMinutes: 180, assistancePlanId: 12, hourTypeId: 7}
      ],
      goals: [
        {
          id: 201,
          title: 'Ziel A',
          description: 'Beschreibung',
          assistancePlanId: 12,
          institutionId: 5,
          hours: [
            {id: 301, weeklyMinutes: 45, goalHourId: 201, hourTypeId: 7}
          ]
        }
      ]
    };

    const result = mapAssistancePlanDtoToUpdateDto(plan);

    expect(result.id).toBe(12);
    expect(result.clientId).toBe(9);
    expect(result.hours[0].id).toBe(101);
    expect(result.hours[0].assistancePlanId).toBe(12);
    expect(result.goals[0].id).toBe(201);
    expect(result.goals[0].hours[0].id).toBe(301);
    expect(result.goals[0].hours[0].goalHourId).toBe(201);
  });

  it('loads existing plans in edit without filtering current plan and sorts by start', () => {
    const assistancePlanService = {
      getExistingByClientId: () => of([
        {id: 5, start: '2026-03-01', end: '2026-03-31', sponsorName: 'B'},
        {id: 4, start: '2026-01-01', end: '2026-01-31', sponsorName: 'A'}
      ] as AssistancePlanExistingDto[])
    };

    const component = new AssistancePlanEditComponent(
      {allValues$: of([])} as any,
      {allValues$: of([])} as any,
      assistancePlanService as any,
      {openSnackBar: () => {}} as any,
      {back: () => {}} as any,
      {snapshot: {paramMap: {get: () => '4'}}} as any,
      {getById: () => of({id: 1, firstName: 'Max', lastName: 'Meier'})} as any,
      {affiliatedInstitutions$: of([]), isAdmin$: of(false)} as any,
      {formatDate: () => '', formatDateToGerman: () => ''} as any
    );
    component.planId = 4;

    (component as any).loadExistingPlans(1);

    expect(component.existingPlans.map(plan => plan.id)).toEqual([4, 5]);
  });

  it('marks currently edited plan as highlighted', () => {
    const component = new AssistancePlanEditComponent(
      {allValues$: of([])} as any,
      {allValues$: of([])} as any,
      {getExistingByClientId: () => of([])} as any,
      {openSnackBar: () => {}} as any,
      {back: () => {}} as any,
      {snapshot: {paramMap: {get: () => '4'}}} as any,
      {getById: () => of({id: 1, firstName: 'Max', lastName: 'Meier'})} as any,
      {affiliatedInstitutions$: of([]), isAdmin$: of(false)} as any,
      {formatDate: () => '', formatDateToGerman: () => ''} as any
    );
    component.planId = 4;

    expect(component.isEditedPlan({id: 4} as AssistancePlanExistingDto)).toBe(true);
    expect(component.isEditedPlan({id: 5} as AssistancePlanExistingDto)).toBe(false);
  });
});
