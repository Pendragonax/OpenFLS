import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of} from 'rxjs';
import {vi} from 'vitest';

import {ContingentOverviewComponent} from './contingent-overview.component';
import {ContingentEvaluationService} from './services/contingent-evaluation.service';
import {MatDialog} from '@angular/material/dialog';
import {CsvService} from '../../../../../shared/services/csv.service';

describe('ContingentOverviewComponent', () => {
  let component: ContingentOverviewComponent;
  let fixture: ComponentFixture<ContingentOverviewComponent>;

  const contingentEvaluationService = {
    getOverviewByInstitutionIdAndYear: vi.fn(() => of({
      employees: []
    }))
  };

  beforeEach(async () => {
    contingentEvaluationService.getOverviewByInstitutionIdAndYear.mockClear();

    await TestBed.configureTestingModule({
      declarations: [ContingentOverviewComponent],
      providers: [
        {provide: ContingentEvaluationService, useValue: contingentEvaluationService},
        {provide: MatDialog, useValue: {open: vi.fn()}},
        {provide: CsvService, useValue: {exportToCsvWithHeader: vi.fn()}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ContingentOverviewComponent);
    component = fixture.componentInstance;
    component.institutionId = 9;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loadValues_showArchivedEmployeesTrue_requestsArchivedEmployees', () => {
    expect(contingentEvaluationService.getOverviewByInstitutionIdAndYear)
      .toHaveBeenCalledWith(9, component.year, false);

    component.showArchivedEmployees = true;
    component.ngOnChanges({
      showArchivedEmployees: {
        currentValue: true,
        previousValue: false,
        firstChange: false,
        isFirstChange: () => false
      }
    } as any);

    expect(contingentEvaluationService.getOverviewByInstitutionIdAndYear)
      .toHaveBeenLastCalledWith(9, component.year, true);
  });

  it('onArchivedVisibilityChanged_emitsSelectionChange', () => {
    const emitted: boolean[] = [];
    component.showArchivedEmployeesChange.subscribe(value => emitted.push(value));

    component.onArchivedVisibilityChanged(true);

    expect(emitted).toEqual([true]);
  });

  it('getData_marksArchivedEmployeesWithArchivedRowClass', () => {
    component.contingentOverView = {
      employees: [
        {
          firstname: 'Anna',
          lastname: 'Archive',
          archived: true,
          contingentHours: [1],
          executedHours: [1],
          executedPercent: [100],
          summedExecutedPercent: [100],
          missingHours: [0],
          absenceDays: [0]
        }
      ]
    } as any;
    component.selectedHourType = 1;

    component.getData();

    expect(component.rowClasses).toEqual(['contingent-row--archived']);
  });
});
