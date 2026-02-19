import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalSingleComponent } from './goal-single.component';

describe('GoalSingleComponent', () => {
  let component: GoalSingleComponent;
  let fixture: ComponentFixture<GoalSingleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GoalSingleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GoalSingleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
