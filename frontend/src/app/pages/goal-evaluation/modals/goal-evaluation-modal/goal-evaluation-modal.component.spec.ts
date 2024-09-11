import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalEvaluationModalComponent } from './goal-evaluation-modal.component';

describe('GoalEvaluationModalComponent', () => {
  let component: GoalEvaluationModalComponent;
  let fixture: ComponentFixture<GoalEvaluationModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GoalEvaluationModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GoalEvaluationModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
