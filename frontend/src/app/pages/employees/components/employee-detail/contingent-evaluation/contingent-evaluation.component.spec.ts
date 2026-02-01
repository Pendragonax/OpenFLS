import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';

import { ContingentEvaluationComponent } from './contingent-evaluation.component';

describe('ContingentEvaluationComponent', () => {
  let component: ContingentEvaluationComponent;
  let fixture: ComponentFixture<ContingentEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatMenuModule],
      declarations: [ContingentEvaluationComponent]
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
