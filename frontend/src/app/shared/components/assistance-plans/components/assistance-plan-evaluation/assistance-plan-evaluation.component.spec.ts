import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of} from 'rxjs';
import {AssistancePlanEvaluationComponent} from './assistance-plan-evaluation.component';
import {ServiceService} from '../../../../services/service.service';
import {EmployeeService} from '../../../../services/employee.service';
import {CsvService} from '../../../../services/csv.service';
import {Converter} from '../../../../services/converter.helper';
import {AssistancePlanHourMode} from '../../../../dtos/assistance-plan-hour-mode.model';

class MockServiceService {
  getByAssistancePlan() {
    return of([]);
  }
}

class MockEmployeeService {
  allValues$ = of([]);
}

class MockCsvService {
  exportToCsv() {}
}

class MockConverter {
  formatDateToGerman(date: Date) {
    return date.toISOString();
  }
  formatDate(date: Date) {
    return date.toISOString();
  }
  getDaysOfMonth() {
    return 1;
  }
}

describe('AssistancePlanEvaluationComponent', () => {
  let component: AssistancePlanEvaluationComponent;
  let fixture: ComponentFixture<AssistancePlanEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AssistancePlanEvaluationComponent],
      providers: [
        {provide: ServiceService, useClass: MockServiceService},
        {provide: EmployeeService, useClass: MockEmployeeService},
        {provide: CsvService, useClass: MockCsvService},
        {provide: Converter, useClass: MockConverter}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssistancePlanEvaluationComponent);
    component = fixture.componentInstance;
    component.assistancePlan = {
      id: 7,
      client: {firstName: 'Max', lastName: 'Meyer'} as any,
      sponsor: {id: 1} as any,
      institution: {id: 1} as any,
      hourMode: AssistancePlanHourMode.CORRIDOR,
      hourCorridor: {
        id: 3,
        title: 'Korridor A',
        weeklyMinutesFrom: 240,
        weeklyMinutesTill: 300,
        hourTypeId: 1,
        hourTypeTitle: 'Pflege'
      },
      hours: [],
      goals: []
    } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the corridor summary for corridor plans', () => {
    expect(fixture.nativeElement.textContent).toContain('Korridor');
    expect(fixture.nativeElement.textContent).toContain('Korridor A');
  });
});
