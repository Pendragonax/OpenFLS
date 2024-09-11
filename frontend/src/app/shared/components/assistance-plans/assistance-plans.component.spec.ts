import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssistancePlansComponent } from './assistance-plans.component';

describe('AssistancePlansComponent', () => {
  let component: AssistancePlansComponent;
  let fixture: ComponentFixture<AssistancePlansComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AssistancePlansComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
