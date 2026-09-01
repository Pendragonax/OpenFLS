import {AssistancePlanProgress} from './assistance-plan-progress.helper';
import {AssistancePlanPreviewDto} from '../dtos/assistance-plan-preview-dto.model';
import {AssistancePlanHourMode} from '../dtos/assistance-plan-hour-mode.model';

function preview(overrides: Partial<AssistancePlanPreviewDto> = {}): AssistancePlanPreviewDto {
  return Object.assign(new AssistancePlanPreviewDto(), overrides);
}

describe('AssistancePlanProgress', () => {

  it('reads exact plans as a share of the approved hours', () => {
    const value = preview({
      hourMode: AssistancePlanHourMode.EXACT,
      approvedHoursThisAssistancePlan: 100,
      executedHoursThisAssistancePlan: 50
    });

    expect(AssistancePlanProgress.getExecutedHoursPercent(value, 'assistancePlan')).toBe(50);
    expect(AssistancePlanProgress.getExecutedHoursProgressClass(value, 'assistancePlan'))
      .toBe('hours-progress-fill--bad');
  });

  it('marks exact plans close to the approved hours as ok', () => {
    const value = preview({
      hourMode: AssistancePlanHourMode.EXACT,
      approvedHoursThisYear: 100,
      executedHoursThisYear: 96
    });

    expect(AssistancePlanProgress.getExecutedHoursProgressClass(value, 'year'))
      .toBe('hours-progress-fill--ok');
  });

  it('warns between 90 and 95 percent for exact plans', () => {
    const value = preview({
      hourMode: AssistancePlanHourMode.EXACT,
      approvedHoursThisYear: 100,
      executedHoursThisYear: 92
    });

    expect(AssistancePlanProgress.getExecutedHoursProgressClass(value, 'year'))
      .toBe('hours-progress-fill--warn');
  });

  it('returns zero percent for exact plans without approved hours', () => {
    const value = preview({
      hourMode: AssistancePlanHourMode.EXACT,
      approvedHoursThisYear: 0,
      executedHoursThisYear: 10
    });

    expect(AssistancePlanProgress.getExecutedHoursPercent(value, 'year')).toBe(0);
  });

  it('reads corridor plans as inside, below or above their corridor', () => {
    const inside = preview({
      hourMode: AssistancePlanHourMode.CORRIDOR,
      approvedHoursThisYearFrom: 80,
      approvedHoursThisYearTill: 120,
      executedHoursThisYear: 100
    });
    const below = preview({
      hourMode: AssistancePlanHourMode.CORRIDOR,
      approvedHoursThisYearFrom: 80,
      approvedHoursThisYearTill: 120,
      executedHoursThisYear: 40
    });
    const above = preview({
      hourMode: AssistancePlanHourMode.CORRIDOR,
      approvedHoursThisYearFrom: 80,
      approvedHoursThisYearTill: 120,
      executedHoursThisYear: 200
    });

    expect(AssistancePlanProgress.getExecutedHoursPercent(inside, 'year')).toBe(50);
    expect(AssistancePlanProgress.getExecutedHoursProgressClass(inside, 'year'))
      .toBe('hours-progress-fill--ok');
    expect(AssistancePlanProgress.getExecutedHoursPercent(below, 'year')).toBeLessThan(40);
    expect(AssistancePlanProgress.getExecutedHoursProgressClass(below, 'year'))
      .toBe('hours-progress-fill--bad');
    expect(AssistancePlanProgress.getExecutedHoursPercent(above, 'year')).toBeGreaterThan(60);
    expect(AssistancePlanProgress.getExecutedHoursProgressClass(above, 'year'))
      .toBe('hours-progress-fill--bad');
  });

  it('shows the corridor range as weekly hours and a single value for exact plans', () => {
    const corridor = preview({
      hourMode: AssistancePlanHourMode.CORRIDOR,
      approvedHoursFrom: 2,
      approvedHoursTo: 4
    });
    const exact = preview({
      hourMode: AssistancePlanHourMode.EXACT,
      approvedHoursPerWeek: 3.5
    });

    expect(AssistancePlanProgress.getWeeklyHoursDisplay(corridor)).toBe('2 - 4');
    expect(AssistancePlanProgress.getWeeklyHoursDisplay(exact)).toBe('3,5');
  });

  it('interprets hour values as time doubles', () => {
    const value = preview({
      hourMode: AssistancePlanHourMode.EXACT,
      approvedHoursThisYear: 2.00,
      executedHoursThisYear: 1.30
    });

    // 1 hour 30 minutes out of 2 hours
    expect(AssistancePlanProgress.getExecutedHoursPercent(value, 'year')).toBe(75);
  });

  it('signs the remaining hours', () => {
    const positive = preview({approvedHoursLeftThisYear: 1.5});
    const negative = preview({approvedHoursLeftThisYear: -1.5});

    expect(AssistancePlanProgress.getApprovedHoursLeftDisplay(positive, 'year')).toBe('+1,5');
    expect(AssistancePlanProgress.getApprovedHoursLeftDisplay(negative, 'year')).toBe('-1,5');
  });
});
