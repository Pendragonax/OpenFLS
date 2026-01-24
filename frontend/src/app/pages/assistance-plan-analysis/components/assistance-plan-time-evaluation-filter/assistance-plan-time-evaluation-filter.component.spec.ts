import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanTimeEvaluationFilterComponent } from './assistance-plan-time-evaluation-filter.component';

describe('GoalTimeEvaluationFilterComponent', () => {
  let component: AssistancePlanTimeEvaluationFilterComponent;
  let fixture: ComponentFixture<AssistancePlanTimeEvaluationFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanTimeEvaluationFilterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanTimeEvaluationFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
