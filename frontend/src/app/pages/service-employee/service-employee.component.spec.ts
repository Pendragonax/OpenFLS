import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceEmployeeComponent } from './service-employee.component';

describe('ServiceEmployeeComponent', () => {
  let component: ServiceEmployeeComponent;
  let fixture: ComponentFixture<ServiceEmployeeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceEmployeeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceEmployeeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
