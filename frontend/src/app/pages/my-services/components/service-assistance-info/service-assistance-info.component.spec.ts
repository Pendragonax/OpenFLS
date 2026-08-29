import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ServiceAssistanceInfoComponent } from './service-assistance-info.component';

describe('ServiceAssistanceInfoComponent', () => {
  let component: ServiceAssistanceInfoComponent;
  let fixture: ComponentFixture<ServiceAssistanceInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceAssistanceInfoComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceAssistanceInfoComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('does not render the corridor summary pill anymore', () => {
    component.assistancePlanSelected = true;
    component.evaluation = {
      assistancePlanId: 1,
      hourMode: 'CORRIDOR',
      approvedHoursFrom: 4,
      approvedHoursTo: 6,
      hourTypeEvaluation: []
    } as any;
    component.info = [{hourTypeName: 'Pflege', leftThisWeek: 1, leftThisMonth: 1, leftThisYear: 1, leftComplete: 1}];

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Korridor');
    expect(fixture.nativeElement.textContent).not.toContain('4 h - 6 h');
  });
});
