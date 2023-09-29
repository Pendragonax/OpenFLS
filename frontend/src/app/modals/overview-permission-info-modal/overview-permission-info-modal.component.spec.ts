import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewPermissionInfoModalComponent } from './overview-permission-info-modal.component';

describe('OverviewPermissionInfoModalComponent', () => {
  let component: OverviewPermissionInfoModalComponent;
  let fixture: ComponentFixture<OverviewPermissionInfoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OverviewPermissionInfoModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OverviewPermissionInfoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
