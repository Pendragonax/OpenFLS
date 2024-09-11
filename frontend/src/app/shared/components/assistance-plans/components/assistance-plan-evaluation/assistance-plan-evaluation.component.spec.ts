import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanEvaluationComponent } from './assistance-plan-evaluation.component';

describe('AssistancePlanEvaluationComponent', () => {
  let component: AssistancePlanEvaluationComponent;
  let fixture: ComponentFixture<AssistancePlanEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanEvaluationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanEvaluationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
