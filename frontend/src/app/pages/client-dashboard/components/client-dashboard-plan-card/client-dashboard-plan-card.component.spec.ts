import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ClientDashboardPlanCardComponent} from './client-dashboard-plan-card.component';
import {Converter} from '../../../../shared/services/converter.helper';
import {AssistancePlanPreviewDto} from '../../../../shared/dtos/assistance-plan-preview-dto.model';

function preview(overrides: Partial<AssistancePlanPreviewDto> = {}): AssistancePlanPreviewDto {
  return Object.assign(new AssistancePlanPreviewDto(), {
    id: 1,
    start: '2026-01-01',
    end: '2026-12-31',
    isActive: true,
    ...overrides
  });
}

describe('ClientDashboardPlanCardComponent', () => {
  let component: ClientDashboardPlanCardComponent;
  let fixture: ComponentFixture<ClientDashboardPlanCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ClientDashboardPlanCardComponent],
      providers: [
        {provide: Converter, useValue: {getLocalDateString: (value: string) => value}}
      ]
    })
      .overrideComponent(ClientDashboardPlanCardComponent, {set: {template: ''}})
      .compileComponents();

    fixture = TestBed.createComponent(ClientDashboardPlanCardComponent);
    component = fixture.componentInstance;
  });

  it('labels a plan running today as "Aktueller Hilfeplan"', () => {
    component.preview = preview({isActive: true});

    expect(component.planTitle).toBe('Aktueller Hilfeplan');
  });

  it('labels a plan that has not started yet as "Nächster Hilfeplan"', () => {
    const future = new Date();
    future.setDate(future.getDate() + 30);
    component.preview = preview({
      isActive: false,
      start: future.toISOString().substring(0, 10),
      end: future.toISOString().substring(0, 10)
    });

    expect(component.planTitle).toBe('Nächster Hilfeplan');
  });

  it('labels an already ended plan as "Aktuellster Hilfeplan"', () => {
    component.preview = preview({isActive: false, start: '2020-01-01', end: '2020-12-31'});

    expect(component.planTitle).toBe('Aktuellster Hilfeplan');
  });

  it('falls back to a neutral title without any plan', () => {
    component.preview = null;

    expect(component.planTitle).toBe('Hilfeplan');
  });
});
