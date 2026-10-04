import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HourReportValueTypeInfoModalComponent } from './hour-report-value-type-info-modal.component';

describe('HourReportValueTypeInfoModalComponent', () => {
  let component: HourReportValueTypeInfoModalComponent;
  let fixture: ComponentFixture<HourReportValueTypeInfoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HourReportValueTypeInfoModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HourReportValueTypeInfoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
