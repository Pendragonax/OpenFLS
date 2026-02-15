import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AssistancePlanNewPageComponent} from './assistance-plan-new.component';

describe('AssistancePlanNewPageComponent', () => {
  let component: AssistancePlanNewPageComponent;
  let fixture: ComponentFixture<AssistancePlanNewPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AssistancePlanNewPageComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanNewPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
