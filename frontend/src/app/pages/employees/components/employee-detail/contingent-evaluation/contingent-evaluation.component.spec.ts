import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContingentEvaluationComponent } from './contingent-evaluation.component';

describe('ContingentEvaluationComponent', () => {
  let component: ContingentEvaluationComponent;
  let fixture: ComponentFixture<ContingentEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContingentEvaluationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContingentEvaluationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
