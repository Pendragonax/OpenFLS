import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { YearMonthSelectionComponent } from './year-month-selection.component';

describe('YearMonthSelectionComponent', () => {
  let component: YearMonthSelectionComponent;
  let fixture: ComponentFixture<YearMonthSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ YearMonthSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(YearMonthSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
