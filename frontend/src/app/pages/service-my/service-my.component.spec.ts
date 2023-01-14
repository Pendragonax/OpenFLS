import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceMyComponent } from './service-my.component';

describe('ServiceMyComponent', () => {
  let component: ServiceMyComponent;
  let fixture: ComponentFixture<ServiceMyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceMyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceMyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
