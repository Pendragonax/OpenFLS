import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalTimeEvaluationFilterComponent } from './goal-time-evaluation-filter.component';

describe('GoalTimeEvaluationFilterComponent', () => {
  let component: GoalTimeEvaluationFilterComponent;
  let fixture: ComponentFixture<GoalTimeEvaluationFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GoalTimeEvaluationFilterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GoalTimeEvaluationFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
