import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalEvaluationComponent } from './goal-evaluation.component';

describe('GoalEvaluationComponent', () => {
  let component: GoalEvaluationComponent;
  let fixture: ComponentFixture<GoalEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GoalEvaluationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GoalEvaluationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
