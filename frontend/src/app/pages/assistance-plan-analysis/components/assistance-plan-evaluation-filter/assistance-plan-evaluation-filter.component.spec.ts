import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanEvaluationFilterComponent } from './assistance-plan-evaluation-filter.component';

describe('GoalEvaluationFilterComponent', () => {
  let component: AssistancePlanEvaluationFilterComponent;
  let fixture: ComponentFixture<AssistancePlanEvaluationFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanEvaluationFilterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanEvaluationFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
