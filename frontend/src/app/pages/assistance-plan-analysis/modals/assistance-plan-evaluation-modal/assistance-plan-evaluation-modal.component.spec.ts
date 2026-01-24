import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanEvaluationModalComponent } from './assistance-plan-evaluation-modal.component';

describe('GoalEvaluationModalComponent', () => {
  let component: AssistancePlanEvaluationModalComponent;
  let fixture: ComponentFixture<AssistancePlanEvaluationModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanEvaluationModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanEvaluationModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
