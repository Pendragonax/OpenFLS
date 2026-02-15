import {of, ReplaySubject} from 'rxjs';
import {describe, expect, it, vi} from 'vitest';
import {AssistancePlansComponent} from './assistance-plans.component';
import {AssistancePlanPreviewDto} from '../../../shared/dtos/assistance-plan-preview-dto.model';

function preview(overrides: Partial<AssistancePlanPreviewDto>): AssistancePlanPreviewDto {
  return {
    id: 0,
    clientId: 0,
    institutionId: 0,
    sponsorId: 0,
    clientFirstname: '',
    clientLastname: '',
    institutionName: '',
    sponsorName: '',
    start: '2026-01-01',
    end: '2026-12-31',
    isActive: true,
    isFavorite: false,
    approvedHoursPerWeek: 0,
    approvedHoursThisYear: 0,
    executedHoursThisYear: 0,
    ...overrides
  };
}

describe('AssistancePlansComponent', () => {
  function createComponent() {
    const assistancePlanService = {
      getPreviewByClientId: vi.fn().mockReturnValue(of([])),
      getPreviewByInstitutionId: vi.fn().mockReturnValue(of([])),
      getPreviewBySponsorId: vi.fn().mockReturnValue(of([])),
      getPreviewByFavorites: vi.fn().mockReturnValue(of([])),
      delete: vi.fn().mockReturnValue(of({}))
    };

    const employeeService = {
      addAssistancePlanFavorite: vi.fn().mockReturnValue(of({})),
      deleteAssistancePlanFavorite: vi.fn().mockReturnValue(of({}))
    };

    const component = new AssistancePlansComponent(
      {} as any,
      {openSnackBar: vi.fn()} as any,
      assistancePlanService as any,
      {allValues$: of([])} as any,
      {getCountByAssistancePlanId: vi.fn().mockReturnValue(of(0))} as any,
      employeeService as any,
      {affiliatedInstitutions$: of([]), isAdmin$: of(false)} as any,
      {compare: (a: any, b: any, isAsc: boolean) => (a < b ? -1 : a > b ? 1 : 0) * (isAsc ? 1 : -1)} as any,
      {open: vi.fn()} as any,
      {getLocalDateString: (d: string | null) => d ?? ''} as any
    );

    return {component, assistancePlanService, employeeService};
  }

  it('filters preview rows by search string across client, institution and sponsor fields', () => {
    const {component} = createComponent();

    component.tableData = [
      {preview: preview({id: 1, clientFirstname: 'Anna', clientLastname: 'Meyer', institutionName: 'Nord', sponsorName: 'AOK'}), editable: true},
      {preview: preview({id: 2, clientFirstname: 'Ben', clientLastname: 'Schulz', institutionName: 'Sued', sponsorName: 'TK'}), editable: true}
    ];

    component.searchString = 'meyer';
    component.filterTableData();

    expect(component.filteredTableData).toHaveLength(1);
    expect(component.filteredTableData[0].preview.id).toBe(1);
  });

  it('re-loads current client context after adding favorite', () => {
    const {component, assistancePlanService} = createComponent();

    (component as any).currentContext = 'client';
    (component as any).currentContextId = 77;

    component.addAssistancePlanAsFavorite(123);

    expect(assistancePlanService.getPreviewByClientId).toHaveBeenCalledWith(77);
  });

  it('re-loads favorites context after deleting favorite', () => {
    const {component, assistancePlanService} = createComponent();

    (component as any).currentContext = 'favorites';
    (component as any).openFavoriteDeleteConfirmationModal = (operation: () => void) => operation();

    component.deleteAssistancePlanAsFavorite(123);

    expect(assistancePlanService.getPreviewByFavorites).toHaveBeenCalledTimes(1);
  });

  it('loads preview rows for client input stream', () => {
    const {component, assistancePlanService} = createComponent();
    const client$ = new ReplaySubject<any>(1);
    component.client$ = client$;

    component.loadValues();
    client$.next({dto: {id: 5}});

    expect(assistancePlanService.getPreviewByClientId).toHaveBeenCalledWith(5);
  });
});
