import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkTimeCardComponent } from './work-time-card.component';

describe('WorkTimeCardComponent', () => {
  let component: WorkTimeCardComponent;
  let fixture: ComponentFixture<WorkTimeCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WorkTimeCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkTimeCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
