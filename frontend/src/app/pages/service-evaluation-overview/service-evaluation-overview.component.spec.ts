import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceEvaluationOverviewComponent } from './service-evaluation-overview.component';

describe('ServiceEvaluationOverviewComponent', () => {
  let component: ServiceEvaluationOverviewComponent;
  let fixture: ComponentFixture<ServiceEvaluationOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceEvaluationOverviewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceEvaluationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
