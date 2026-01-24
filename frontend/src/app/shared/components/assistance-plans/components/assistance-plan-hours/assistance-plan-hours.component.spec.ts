import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanHoursComponent } from './assistance-plan-hours.component';

describe('AssistancePlanHoursComponent', () => {
  let component: AssistancePlanHoursComponent;
  let fixture: ComponentFixture<AssistancePlanHoursComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanHoursComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanHoursComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
