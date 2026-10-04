import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {of, throwError} from 'rxjs';
import {vi} from 'vitest';

import {HomeClientFavoritesComponent} from './home-client-favorites.component';
import {ClientDashboardService} from '../../../../shared/services/client-dashboard.service';
import {HelperService} from '../../../../shared/services/helper.service';
import {Converter} from '../../../../shared/services/converter.helper';
import {ClientFavoriteDto} from '../../../../shared/dtos/client-dashboard-dto.model';

function favorite(overrides: Partial<ClientFavoriteDto> = {}): ClientFavoriteDto {
  return {
    clientId: 1,
    firstName: 'Max',
    lastName: 'Mustermann',
    archived: false,
    institutionId: 5,
    institutionName: 'Nord',
    hasActiveAssistancePlan: true,
    assistancePlanEnd: '2026-12-31',
    openTaskCount: 0,
    overdueTaskCount: 0,
    ...overrides
  };
}

describe('HomeClientFavoritesComponent', () => {
  let component: HomeClientFavoritesComponent;
  let fixture: ComponentFixture<HomeClientFavoritesComponent>;
  let clientDashboardService: {
    getFavorites: ReturnType<typeof vi.fn>;
    deleteFavorite: ReturnType<typeof vi.fn>;
  };
  let helperService: { openSnackBar: ReturnType<typeof vi.fn> };
  let matDialog: { open: ReturnType<typeof vi.fn> };

  /** Mocks the next MatDialog.open() call and resolves afterClosed() with `confirmed`. */
  function mockConfirmationDialog(confirmed: boolean | undefined) {
    matDialog.open.mockReturnValueOnce({
      componentInstance: {},
      afterClosed: () => of(confirmed)
    });
  }

  beforeEach(async () => {
    clientDashboardService = {
      getFavorites: vi.fn(() => of([
        favorite(),
        favorite({clientId: 2, firstName: 'Mia', lastName: 'Musterfrau', institutionName: 'Süd'})
      ])),
      deleteFavorite: vi.fn(() => of(undefined))
    };
    helperService = {openSnackBar: vi.fn()};
    matDialog = {open: vi.fn()};

    await TestBed.configureTestingModule({
      declarations: [HomeClientFavoritesComponent],
      providers: [
        {provide: ClientDashboardService, useValue: clientDashboardService},
        {provide: HelperService, useValue: helperService},
        {provide: Converter, useValue: {getLocalDateString: (value: string) => value}},
        {provide: MatDialog, useValue: matDialog}
      ]
    })
      .overrideComponent(HomeClientFavoritesComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(HomeClientFavoritesComponent);
    component = fixture.componentInstance;
  });

  it('loads the favourite clients of the signed in employee', () => {
    fixture.detectChanges();

    expect(clientDashboardService.getFavorites).toHaveBeenCalled();
    expect(component.favorites.length).toBe(2);
    expect(component.loadFailed).toBe(false);
  });

  it('filters by name and institution', () => {
    fixture.detectChanges();

    component.onSearchStringChanges('süd');

    expect(component.filteredFavorites.map(value => value.clientId)).toEqual([2]);

    component.onSearchStringChanges('musterfrau');

    expect(component.filteredFavorites.map(value => value.clientId)).toEqual([2]);
  });

  it('asks for confirmation and removes a favourite without navigating once confirmed', () => {
    fixture.detectChanges();
    const event = {stopPropagation: vi.fn(), preventDefault: vi.fn()} as unknown as Event;
    mockConfirmationDialog(true);

    component.removeFavorite(component.favorites[0], event);

    expect(event.stopPropagation).toHaveBeenCalled();
    expect(event.preventDefault).toHaveBeenCalled();
    expect(clientDashboardService.deleteFavorite).toHaveBeenCalledWith(1);
    expect(component.favorites.map(value => value.clientId)).toEqual([2]);
  });

  it('keeps the favourite when the removal is not confirmed', () => {
    fixture.detectChanges();
    const event = {stopPropagation: vi.fn(), preventDefault: vi.fn()} as unknown as Event;
    mockConfirmationDialog(false);

    component.removeFavorite(component.favorites[0], event);

    expect(clientDashboardService.deleteFavorite).not.toHaveBeenCalled();
    expect(component.favorites.map(value => value.clientId)).toEqual([1, 2]);
  });

  it('marks a failed load', () => {
    clientDashboardService.getFavorites.mockReturnValueOnce(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.loadFailed).toBe(true);
    expect(component.isLoading).toBe(false);
  });

  it('describes the task badge tooltip depending on open and overdue counts', () => {
    expect(component.getTaskTooltip(favorite({openTaskCount: 0, overdueTaskCount: 0})))
      .toBe('Keine offenen Aufgaben');
    expect(component.getTaskTooltip(favorite({openTaskCount: 3, overdueTaskCount: 0})))
      .toBe('3 offene Aufgaben');
    expect(component.getTaskTooltip(favorite({openTaskCount: 3, overdueTaskCount: 2})))
      .toBe('3 offene Aufgaben, davon 2 überfällig');
  });
});
