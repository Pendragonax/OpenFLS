import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanAnalysisComponent } from './assistance-plan-analysis.component';

describe('AsssitancePlanAnalysisComponent', () => {
  let component: AssistancePlanAnalysisComponent;
  let fixture: ComponentFixture<AssistancePlanAnalysisComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanAnalysisComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
