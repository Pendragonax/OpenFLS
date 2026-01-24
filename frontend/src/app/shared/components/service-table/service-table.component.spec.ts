import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceTableComponent } from './service-table.component';

describe('ServiceViewTableComponent', () => {
  let component: ServiceTableComponent;
  let fixture: ComponentFixture<ServiceTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServiceTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
