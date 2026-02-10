import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ServiceAssistanceInfoComponent } from './service-assistance-info.component';

describe('ServiceAssistanceInfoComponent', () => {
  let component: ServiceAssistanceInfoComponent;
  let fixture: ComponentFixture<ServiceAssistanceInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceAssistanceInfoComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ServiceAssistanceInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
