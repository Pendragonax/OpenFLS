import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalEvaluationFilterComponent } from './goal-evaluation-filter.component';

describe('GoalEvaluationFilterComponent', () => {
  let component: GoalEvaluationFilterComponent;
  let fixture: ComponentFixture<GoalEvaluationFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GoalEvaluationFilterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GoalEvaluationFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
