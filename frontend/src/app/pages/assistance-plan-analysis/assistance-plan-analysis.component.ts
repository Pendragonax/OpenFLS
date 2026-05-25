import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {combineLatest, ReplaySubject} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AssistancePlanService} from '../../shared/services/assistance-plan.service';
import {ClientsService} from '../../shared/services/clients.service';
import {DateService} from '../../shared/services/date.service';
import {EvaluationsService} from '../../shared/services/evaluations.service';
import {GoalTimeEvaluationService} from '../../shared/services/goal-time-evaluation.service';
import {PeriodMode} from '../../shared/components/year-month-selection/PeriodMode';
import {Period} from '../../shared/components/year-month-selection/Period';
import {TableButtonCell} from '../../shared/components/table-button/TableButtonCell';
import {AssistancePlanEvaluationModalComponent} from './modals/assistance-plan-evaluation-modal/assistance-plan-evaluation-modal.component';
import {GoalsTimeEvaluationDto} from '../../shared/dtos/goals-time-evaluation-dto.model';
import {GoalTimeEvaluationDto} from '../../shared/dtos/goal-time-evaluation-dto.model';
import {EAssistancePlanEvaluationType} from './components/assistance-plan-time-evaluation-filter/EAssistancePlanEvaluationType';
import {GoalEvaluationYearDto} from '../../shared/dtos/goal-evaluation-year-dto.model';
import {EvaluationDto} from '../../shared/dtos/evaluation-dto.model';
import {AssistancePlan} from '../../shared/projections/assistance-plan.projection';

@Component({
  selector: 'app-assistance-plan-analysis',
  templateUrl: './assistance-plan-analysis.component.html',
  styleUrls: ['./assistance-plan-analysis.component.css'],
  standalone: false
})
export class AssistancePlanAnalysisComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  private validTabIndices = [0, 1, 2];
  private tabParamName = 'tab';
  private idParamName = 'id';

  columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>();
  columnsEvaluation$: ReplaySubject<string[]> = new ReplaySubject<string[]>();
  data$: ReplaySubject<string[][]> = new ReplaySubject<string[][]>();
  dataEvaluation$: ReplaySubject<TableButtonCell[][]> = new ReplaySubject<TableButtonCell[][]>();
  columnFixedWidthFromIndex$: ReplaySubject<number> = new ReplaySubject<number>();
  boldColumnIndices$: ReplaySubject<number[]> = new ReplaySubject<number[]>();
  isGenerating$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

  assistancePlanId = 0;
  assistancePlan: AssistancePlan = new AssistancePlan();
  clientArchived = false;
  evaluations: GoalEvaluationYearDto = new GoalEvaluationYearDto();
  goalTimesEvaluation: GoalsTimeEvaluationDto = new GoalsTimeEvaluationDto();
  selectedGoalEvaluationHourType: EAssistancePlanEvaluationType | null = null;
  selectedGoalTimeHourTypeId = 0;
  selectedGoalTimeYear = 0;
  selectedEvaluationYear = 0;
  tabIndex = 0;

  isGenerating = false;
  errorOccurred = true;
  goalTimesErrorOccurred = true;

  constructor(
    private assistancePlanService: AssistancePlanService,
    private goalTimeEvaluationService: GoalTimeEvaluationService,
    private evaluationService: EvaluationsService,
    private clientService: ClientsService,
    private dateService: DateService,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.executeURLParams();
    combineLatest([
      this.assistancePlanService.getProjectionById(this.assistancePlanId),
      this.clientService.allValues$
    ])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([assistancePlan, clients]) => {
        this.assistancePlan = assistancePlan;
        this.clientArchived = clients.some(client => client.id === assistancePlan.client.id && client.archived);
        this.loadValues();
        this.onEvaluationPeriodChanged(new Period(PeriodMode.PERIOD_MODE_YEARLY, new Date().getFullYear(), 1));
      });
  }

  onGoalTimeEvaluationTypeChanged(type: EAssistancePlanEvaluationType) {
    this.selectedGoalEvaluationHourType = type;
    this.updateGoalTimeTable();
  }

  onGoalTimeHourTypeChanged(hourTypeId: number) {
    this.selectedGoalTimeHourTypeId = hourTypeId;
    this.loadGoalTimes();
  }

  onEvaluationPeriodChanged(period: Period) {
    switch (period.periodMode) {
      case PeriodMode.PERIOD_MODE_YEARLY:
        this.selectedEvaluationYear = period.year;
        this.loadEvaluations();
        break;
      default:
        return;
    }
  }

  loadEvaluations() {
    this.errorOccurred = false;
    this.isGenerating = true;
    this.isGenerating$.next(this.isGenerating);

    this.evaluationService.getByAssistancePlanIdAndYear(this.assistancePlanId, this.selectedEvaluationYear)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: value => {
          const rows = value.values.map(it => this.getTableButtonsAsRow(it, this.selectedEvaluationYear));
          this.dataEvaluation$.next(rows);
          this.columnsEvaluation$.next(this.dateService.getMonths(['Name']));
          this.errorOccurred = false;
          this.isGenerating = false;
          this.isGenerating$.next(this.isGenerating);
        },
        error: () => {
          this.errorOccurred = true;
          this.isGenerating = false;
          this.isGenerating$.next(this.isGenerating);
        }
      });
  }

  loadGoalTimes() {
    if (this.selectedGoalTimeHourTypeId <= 0 || this.assistancePlanId <= 0) {
      this.goalTimesErrorOccurred = true;
      return;
    }

    this.goalTimesErrorOccurred = false;
    this.isGenerating = true;
    this.isGenerating$.next(this.isGenerating);

    this.goalTimeEvaluationService
      .getByYear(this.assistancePlanId, this.selectedGoalTimeHourTypeId, this.selectedGoalTimeYear)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (value) => {
          this.goalTimesEvaluation = value;
          this.updateGoalTimeTable();
          this.isGenerating = false;
          this.isGenerating$.next(this.isGenerating);
        },
        error: () => {
          this.isGenerating = false;
          this.isGenerating$.next(this.isGenerating);
          this.goalTimesErrorOccurred = true;
        }
      });
  }

  onGoalTimePeriodChanged(period: Period) {
    this.selectedGoalTimeYear = period.year;
    this.loadGoalTimes();
  }

  updateGoalTimeTable() {
    let rows = this.goalTimesEvaluation.goalTimeEvaluations
      .map(it => this.getGoalTimesAsRow(it, this.selectedGoalEvaluationHourType));

    rows = [...rows, this.getGoalsTimeAsRow(this.goalTimesEvaluation, this.selectedGoalEvaluationHourType)];
    this.data$.next(rows);
    this.columns$.next(this.dateService.getMonths(['Name']));
  }

  loadValues() {
    this.columnFixedWidthFromIndex$.next(1);
    this.boldColumnIndices$.next([0]);
  }

  getGoalsTimeAsRow(goalsTime: GoalsTimeEvaluationDto, type: EAssistancePlanEvaluationType | null): string[] {
    const cells = ['Hilfeplan'];

    for (let i = 0; i < goalsTime.approvedHours.length; i++) {
      switch (type) {
        case EAssistancePlanEvaluationType.Approved:
          cells.push(goalsTime.approvedHours[i] <= 0 ? '-' : goalsTime.approvedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedApproved:
          cells.push(goalsTime.summedApprovedHours[i] <= 0 ? '-' : goalsTime.summedApprovedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Executed:
          cells.push(goalsTime.executedHours[i] <= 0 ? '-' : goalsTime.executedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedExecuted:
          cells.push(goalsTime.summedExecutedHours[i] <= 0 ? '-' : goalsTime.summedExecutedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Left:
          cells.push(goalsTime.approvedHoursLeft[i] <= 0 ? '-' : goalsTime.approvedHoursLeft[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedLeft:
          cells.push(goalsTime.summedApprovedHoursLeft[i] <= 0 ? '-' : goalsTime.summedApprovedHoursLeft[i].toFixed(2).toString());
          break;
        default:
          break;
      }
    }

    return cells;
  }

  getGoalTimesAsRow(goalTimes: GoalTimeEvaluationDto, type: EAssistancePlanEvaluationType | null): string[] {
    const cells = [`Ziel: ${goalTimes.title}`];

    for (let i = 0; i < goalTimes.approvedHours.length; i++) {
      switch (type) {
        case EAssistancePlanEvaluationType.Approved:
          cells.push(goalTimes.approvedHours[i] <= 0 ? '-' : goalTimes.approvedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedApproved:
          cells.push(goalTimes.summedApprovedHours[i] <= 0 ? '-' : goalTimes.summedApprovedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Executed:
          cells.push(goalTimes.executedHours[i] <= 0 ? '-' : goalTimes.executedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedExecuted:
          cells.push(goalTimes.summedExecutedHours[i] <= 0 ? '-' : goalTimes.summedExecutedHours[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Left:
          cells.push(goalTimes.approvedHoursLeft[i] <= 0 ? '-' : goalTimes.approvedHoursLeft[i].toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedLeft:
          cells.push(goalTimes.summedApprovedHoursLeft[i] <= 0 ? '-' : goalTimes.summedApprovedHoursLeft[i].toFixed(2).toString());
          break;
        default:
          break;
      }
    }

    return cells;
  }

  getTableButtonsAsRow(goalEvaluationYear: GoalEvaluationYearDto, year: number): TableButtonCell[] {
    const cells: TableButtonCell[] = [
      new TableButtonCell(false, false, false, this.truncateString(goalEvaluationYear.title, 120), 0)
    ];

    for (let i = 0; i < goalEvaluationYear.months.length; i++) {
      const exists = goalEvaluationYear.months[i].evaluation != null;
      const checked = goalEvaluationYear.months[i].evaluation?.approved ?? false;
      const enabled = goalEvaluationYear.months[i].assistancePlanActive;
      const evaluation = goalEvaluationYear.months[i].evaluation;
      const date = new Date(year, i, 1);
      cells.push(new TableButtonCell(exists, checked, enabled, '', {
        goalId: goalEvaluationYear.goalId,
        date,
        evaluation,
        readOnly: this.clientArchived
      }));
    }

    return cells;
  }

  truncateString(inputString: string, maxLength: number): string {
    if (inputString.length <= maxLength) {
      return inputString;
    }

    return inputString.substring(0, maxLength) + '...';
  }

  openEvaluationModal(payload: { goalId: number, date: Date, evaluation: EvaluationDto, readOnly?: boolean }) {
    const dialogRef = this.dialog.open(AssistancePlanEvaluationModalComponent);
    const dialog = dialogRef.componentInstance;
    dialog.evaluation$.next(payload.evaluation);
    dialog.goalId$.next(payload.goalId);
    dialog.date$.next(payload.date);
    dialog.readOnly$.next(payload.readOnly ?? false);
    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: _ => this.loadEvaluations()
      });
  }

  executeURLParams() {
    this.route.params
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        if (params[this.tabParamName]) {
          const urlTabIndex = +params[this.tabParamName];
          if (this.validTabIndices.includes(urlTabIndex, 0)) {
            this.tabIndex = urlTabIndex;
          }
        }

        if (params[this.idParamName]) {
          this.assistancePlanId = +params[this.idParamName];
        }
      });
  }

  updateUrl() {
    this.location.go(`assistance_plans/goals/${this.assistancePlanId}/${this.tabIndex}`);
  }

  protected readonly console = console;
}
