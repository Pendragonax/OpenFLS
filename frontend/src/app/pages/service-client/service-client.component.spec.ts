import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceClientComponent } from './service-client.component';

describe('ServiceClientComponent', () => {
  let component: ServiceClientComponent;
  let fixture: ComponentFixture<ServiceClientComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceClientComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
