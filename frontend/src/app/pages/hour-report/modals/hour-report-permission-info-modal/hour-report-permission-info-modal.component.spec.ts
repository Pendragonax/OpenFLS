import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HourReportPermissionInfoModalComponent } from './hour-report-permission-info-modal.component';

describe('HourReportPermissionInfoModalComponent', () => {
  let component: HourReportPermissionInfoModalComponent;
  let fixture: ComponentFixture<HourReportPermissionInfoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HourReportPermissionInfoModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HourReportPermissionInfoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
