import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlanNewComponent } from './assistance-plan-new.component';

describe('AssistancePlanNewComponent', () => {
  let component: AssistancePlanNewComponent;
  let fixture: ComponentFixture<AssistancePlanNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlanNewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
