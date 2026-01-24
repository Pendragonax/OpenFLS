import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewValueTypeInfoModalComponent } from './overview-value-type-info-modal.component';

describe('OverviewValuetypeInfoModalComponent', () => {
  let component: OverviewValueTypeInfoModalComponent;
  let fixture: ComponentFixture<OverviewValueTypeInfoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OverviewValueTypeInfoModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OverviewValueTypeInfoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
