import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceDetailComponent } from './service-detail.component';

describe('ServiceNewComponent', () => {
  let component: ServiceDetailComponent;
  let fixture: ComponentFixture<ServiceDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
