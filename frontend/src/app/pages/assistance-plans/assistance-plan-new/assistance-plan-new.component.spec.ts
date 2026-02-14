import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AssistancePlanNewSinglePageComponent} from './assistance-plan-new.component';

describe('AssistancePlanNewSinglePageComponent', () => {
  let component: AssistancePlanNewSinglePageComponent;
  let fixture: ComponentFixture<AssistancePlanNewSinglePageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AssistancePlanNewSinglePageComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanNewSinglePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
