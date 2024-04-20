import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContingentOverviewComponent } from './contingent-overview.component';

describe('ContingentOverviewComponent', () => {
  let component: ContingentOverviewComponent;
  let fixture: ComponentFixture<ContingentOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContingentOverviewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContingentOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
