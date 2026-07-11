import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';

import {ContingentOverviewToolbarComponent} from './contingent-overview-toolbar.component';

describe('ContingentOverviewToolbarComponent', () => {
  let component: ContingentOverviewToolbarComponent;
  let fixture: ComponentFixture<ContingentOverviewToolbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ContingentOverviewToolbarComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ContingentOverviewToolbarComponent);
    component = fixture.componentInstance;
    component.showArchivedEmployeesToggleVisible = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('onArchivedVisibilityChanged_emitsSelectionChange', () => {
    const emitted: boolean[] = [];
    component.showArchivedEmployeesChange.subscribe(value => emitted.push(value));

    component.onArchivedVisibilityChanged(true);

    expect(emitted).toEqual([true]);
  });
});
