import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';
import { of } from 'rxjs';
import { ContingentsService } from '../../../../../shared/services/contingents.service';
import { AbsenceService } from '../../../../../shared/services/absence.service';
import { DateService } from '../../../../../shared/services/date.service';
import { UserService } from '../../../../../shared/services/user.service';
import { Router } from '@angular/router';

import { ContingentEvaluationComponent } from './contingent-evaluation.component';

describe('ContingentEvaluationComponent', () => {
  let component: ContingentEvaluationComponent;
  let fixture: ComponentFixture<ContingentEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatMenuModule],
      declarations: [ContingentEvaluationComponent],
      providers: [
        {
          provide: ContingentsService,
          useValue: {
            getCalendarInformation: () => of({ days: [], today: {}, thisWeek: {}, thisMonth: {} })
          }
        },
        { provide: Router, useValue: { navigate: () => Promise.resolve(true) } },
        { provide: DateService, useValue: { formatDateToYearMonthDay: () => '2026-02-15' } },
        { provide: AbsenceService, useValue: { create: () => of({}), remove: () => of({}) } },
        { provide: UserService, useValue: { user$: of({ id: 1 }) } }
      ]
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

  it('marks absent using absence service with selected date', () => {
    const absenceService = TestBed.inject(AbsenceService);
    const createSpy = vi.spyOn(absenceService, 'create').mockReturnValue(of({} as any));
    const loadSpy = vi.spyOn(component, 'loadValues').mockImplementation(() => undefined);
    component.lastSelectedDate = new Date('2026-02-15');

    component.onMarkAbsent();

    expect(createSpy).toHaveBeenCalledWith(component.lastSelectedDate);
    expect(loadSpy).toHaveBeenCalled();
  });
});
