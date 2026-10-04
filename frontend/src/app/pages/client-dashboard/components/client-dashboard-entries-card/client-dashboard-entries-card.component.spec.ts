import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ClientDashboardEntriesCardComponent} from './client-dashboard-entries-card.component';
import {Converter} from '../../../../shared/services/converter.helper';
import {ClientLatestServiceDto} from '../../../../shared/dtos/client-dashboard-dto.model';

function entry(overrides: Partial<ClientLatestServiceDto> = {}): ClientLatestServiceDto {
  return {
    id: 1,
    start: '2026-03-09T09:00:00',
    end: '2026-03-09T10:00:00',
    minutes: 60,
    title: 'Gespräch',
    content: 'Inhalt',
    institutionId: 5,
    institutionName: 'Inst',
    employeeId: 7,
    employeeFirstname: 'Anna',
    employeeLastname: 'Autorin',
    assistancePlanId: 1,
    ...overrides
  };
}

describe('ClientDashboardEntriesCardComponent', () => {
  let component: ClientDashboardEntriesCardComponent;
  let fixture: ComponentFixture<ClientDashboardEntriesCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ClientDashboardEntriesCardComponent],
      providers: [
        {provide: Converter, useValue: {getLocalDateString: (value: string) => value}}
      ]
    })
      .overrideComponent(ClientDashboardEntriesCardComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(ClientDashboardEntriesCardComponent);
    component = fixture.componentInstance;
  });

  it('marks an entry documented by the current employee as own', () => {
    component.currentEmployeeId = 7;

    expect(component.isOwnEntry(entry({employeeId: 7}))).toBe(true);
    expect(component.isOwnEntry(entry({employeeId: 9}))).toBe(false);
  });

  it('does not mark anything as own before the current employee id is known', () => {
    component.currentEmployeeId = 0;

    expect(component.isOwnEntry(entry({employeeId: 7}))).toBe(false);
  });

  it('routes a single entry to the entries of its own day', () => {
    component.clientId = 3;

    expect(component.getDayRouterLink(entry({start: '2026-03-09T09:00:00'})))
      .toEqual(['/services/all', '2026-03-09', '2026-03-09', 0, 0, 3]);
  });
});
