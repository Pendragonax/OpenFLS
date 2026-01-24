import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanDetailComponent } from './assistance-plan-detail.component';

describe('AssistancePlanDetailComponent', () => {
  let component: AssistancePlanDetailComponent;
  let fixture: ComponentFixture<AssistancePlanDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
