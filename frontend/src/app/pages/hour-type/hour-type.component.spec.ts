import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HourTypeComponent } from './hour-type.component';

describe('HourTypeComponent', () => {
  let component: HourTypeComponent;
  let fixture: ComponentFixture<HourTypeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HourTypeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HourTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
