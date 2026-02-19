import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DateCompleteSelectionComponent } from './date-complete-selection.component';

describe('DateCompleteSelectionComponent', () => {
  let component: DateCompleteSelectionComponent;
  let fixture: ComponentFixture<DateCompleteSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateCompleteSelectionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DateCompleteSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
