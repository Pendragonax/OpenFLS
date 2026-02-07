import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Subject, BehaviorSubject, of } from 'rxjs';
import { ServiceEvaluationOverviewComponent } from './service-evaluation-overview.component';
import { HourTypeService } from '../../shared/services/hour-type.service';
import { InstitutionService } from '../../shared/services/institution.service';
import { SponsorService } from '../../shared/services/sponsor.service';
import { OverviewService } from '../../shared/services/overview.service';
import { DateService } from '../../shared/services/date.service';
import { AssistancePlanAnalysisService } from './services/assistance-plan-analysis.service';
import { Converter } from '../../shared/services/converter.helper';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { HourTypeDto } from '../../shared/dtos/hour-type-dto.model';
import { InstitutionDto } from '../../shared/dtos/institution-dto.model';
import { SponsorDto } from '../../shared/dtos/sponsor-dto.model';
import { vi } from 'vitest';

describe('ServiceEvaluationOverviewComponent', () => {
  let component: ServiceEvaluationOverviewComponent;
  let fixture: ComponentFixture<ServiceEvaluationOverviewComponent>;
  let params$: Subject<any>;
  let hourTypes$: BehaviorSubject<HourTypeDto[]>;
  let institutions$: BehaviorSubject<InstitutionDto[]>;
  let sponsors$: BehaviorSubject<SponsorDto[]>;
  let locationGo: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    params$ = new Subject();
    hourTypes$ = new BehaviorSubject([new HourTypeDto({ id: 1, title: 'h' })]);
    institutions$ = new BehaviorSubject([new InstitutionDto({ id: 2, name: 'i' })]);
    sponsors$ = new BehaviorSubject([new SponsorDto({ id: 3, name: 's' })]);
    locationGo = vi.fn();

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ ServiceEvaluationOverviewComponent ],
      providers: [
        { provide: ActivatedRoute, useValue: { params: params$ } },
        { provide: HourTypeService, useValue: { allValues$: hourTypes$ } },
        { provide: InstitutionService, useValue: { allValues$: institutions$ } },
        { provide: SponsorService, useValue: { allValues$: sponsors$ } },
        { provide: OverviewService, useValue: { getOverviewFromAssistancePlanByYear: () => of([]) } },
        { provide: AssistancePlanAnalysisService, useValue: { getByYearAndMonthAndInstitutionIdAndSponsorIdAndHourTypeId: () => of({}), convertToArray: () => [[]] } },
        { provide: DateService, useValue: { getMonths: () => [] } },
        { provide: Converter, useValue: { getLocalDateString: (value: string | null) => value ?? '' } },
        { provide: Location, useValue: { go: locationGo } },
        { provide: MatDialog, useValue: { open: () => ({}) } },
      ],
    })
    .overrideComponent(ServiceEvaluationOverviewComponent, { set: { template: '' } })
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

  it('unsubscribe_afterDestroy_doesNotUpdateState', () => {
    // Given
    fixture.detectChanges();
    component.periodModeControl.setValue('2');

    // When
    locationGo.mockClear();
    const previousPeriodMode = component.selectedPeriodMode;
    fixture.destroy();
    component.periodModeControl.setValue('1');

    // Then
    expect(component.selectedPeriodMode).toBe(previousPeriodMode);
    expect(locationGo).not.toHaveBeenCalled();
  });
});
