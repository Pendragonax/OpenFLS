import {describe, expect, it} from 'vitest';
import {AssistancePlanDto} from '../../../shared/dtos/assistance-plan-dto.model';
import {mapAssistancePlanDtoToUpdateDto} from './assistance-plan-edit.component';

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
});
