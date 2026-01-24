import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {AssistancePlanService} from "../../shared/services/assistance-plan.service";
import {PeriodMode} from "../../shared/components/year-month-selection/PeriodMode";
import {combineLatest, ReplaySubject} from "rxjs";
import {DateService} from "../../shared/services/date.service";
import {Period} from "../../shared/components/year-month-selection/Period";
import {TableButtonCell} from "../../shared/components/table-button/TableButtonCell";
import {MatDialog} from "@angular/material/dialog";
import {AssistancePlanEvaluationModalComponent} from "./modals/assistance-plan-evaluation-modal/assistance-plan-evaluation-modal.component";
import {Location} from "@angular/common";
import {GoalTimeEvaluationService} from "../../shared/services/goal-time-evaluation.service";
import {GoalsTimeEvaluationDto} from "../../shared/dtos/goals-time-evaluation-dto.model";
import {GoalTimeEvaluationDto} from "../../shared/dtos/goal-time-evaluation-dto.model";
import {EAssistancePlanEvaluationType} from "./components/assistance-plan-time-evaluation-filter/EAssistancePlanEvaluationType";
import {EvaluationsService} from "../../shared/services/evaluations.service";
import {GoalEvaluationYearDto} from "../../shared/dtos/goal-evaluation-year-dto.model";
import {EvaluationDto} from "../../shared/dtos/evaluation-dto.model";
import {ClientsService} from "../../shared/services/clients.service";
import {AssistancePlan} from "../../shared/projections/assistance-plan.projection";

@Component({
    selector: 'app-assistance-plan-analysis',
    templateUrl: './assistance-plan-analysis.component.html',
    styleUrls: ['./assistance-plan-analysis.component.css'],
    standalone: false
})
export class AssistancePlanAnalysisComponent implements OnInit {

  // CONST
  private validTabIndices = [0,1,2];
  private tabParamName = 'tab';
  private idParamName = 'id';

  // table
  columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>()
  columnsEvaluation$: ReplaySubject<string[]> = new ReplaySubject<string[]>()
  data$: ReplaySubject<string[][]> = new ReplaySubject()
  dataEvaluation$: ReplaySubject<TableButtonCell[][]> = new ReplaySubject()
  columnFixedWidthFromIndex$: ReplaySubject<number> = new ReplaySubject<number>()
  boldColumnIndices$: ReplaySubject<number[]> = new ReplaySubject<number[]>()
  isGenerating$: ReplaySubject<boolean> = new ReplaySubject()

  // VAR
  assistancePlanId = 0;
  assistancePlan: AssistancePlan = new AssistancePlan();
  evaluations: GoalEvaluationYearDto = new GoalEvaluationYearDto();
  goalTimesEvaluation: GoalsTimeEvaluationDto = new GoalsTimeEvaluationDto();
  selectedGoalEvaluationHourType: EAssistancePlanEvaluationType | null = null;
  selectedGoalTimeHourTypeId: number = 0
  selectedGoalTimeYear: number = 0
  selectedEvaluationYear: number = 0
  tabIndex = 0;

  // STATE
  isGenerating: boolean = false
  errorOccurred: boolean = true
  goalTimesErrorOccurred: boolean = true

  constructor(private assistancePlanService: AssistancePlanService,
              private goalTimeEvaluationService: GoalTimeEvaluationService,
              private evaluationService: EvaluationsService,
              private clientService: ClientsService,
              private dateService: DateService,
              private dialog: MatDialog,
              private route: ActivatedRoute,
              private location: Location) {}

  ngOnInit(): void {
    this.executeURLParams();
    combineLatest([
      this.assistancePlanService.getProjectionById(this.assistancePlanId),
      this.clientService.allValues$
    ]).subscribe(([assistancePlan, clients]) => {
        this.assistancePlan = assistancePlan;
        this.loadValues();
        this.onEvaluationPeriodChanged(new Period(PeriodMode.PERIOD_MODE_YEARLY, new Date().getFullYear(), 1));
      });
  }

  onGoalTimeEvaluationTypeChanged(type: EAssistancePlanEvaluationType) {
    this.selectedGoalEvaluationHourType = type
    this.updateGoalTimeTable()
  }

  onGoalTimeHourTypeChanged(hourTypeId: number) {
    this.selectedGoalTimeHourTypeId = hourTypeId
    this.loadGoalTimes()
  }

  onEvaluationPeriodChanged(period: Period) {
    switch (period.periodMode) {
      case PeriodMode.PERIOD_MODE_YEARLY:
        this.selectedEvaluationYear = period.year
        this.loadEvaluations()
        break;
      default:
        return;
    }
  }

  loadEvaluations() {
    this.errorOccurred = false
    this.isGenerating = true
    this.isGenerating$.next(this.isGenerating)

    this.evaluationService.getByAssistancePlanIdAndYear(this.assistancePlanId, this.selectedEvaluationYear).subscribe({
      next: value => {
        let rows = value.values.map(it => this.getTableButtonsAsRow(it, this.selectedEvaluationYear));
        this.dataEvaluation$.next(rows);
        this.columnsEvaluation$.next(this.dateService.getMonths(["Name"]))
        this.errorOccurred = false
        this.isGenerating = false
        this.isGenerating$.next(this.isGenerating)
      }
    })
  }

  loadGoalTimes() {
    if (this.selectedGoalTimeHourTypeId <= 0 || this.assistancePlanId <= 0) {
      this.goalTimesErrorOccurred = true
      return
    }

    this.goalTimesErrorOccurred = false
    this.isGenerating = true
    this.isGenerating$.next(this.isGenerating)

    this.goalTimeEvaluationService
      .getByYear(this.assistancePlanId, this.selectedGoalTimeHourTypeId, this.selectedGoalTimeYear)
      .subscribe({
        next: (value) => {
          this.goalTimesEvaluation = value
          this.updateGoalTimeTable()
          this.isGenerating = false
          this.isGenerating$.next(this.isGenerating)
        },
        error: _ => {
          this.isGenerating = false
          this.isGenerating$.next(this.isGenerating)
          this.goalTimesErrorOccurred = true
        }
      })
  }

  onGoalTimePeriodChanged(period: Period) {
    this.selectedGoalTimeYear = period.year
    this.loadGoalTimes()
  }

  updateGoalTimeTable() {
    let rows =
      this.goalTimesEvaluation.goalTimeEvaluations
        .map(it => this.getGoalTimesAsRow(it, this.selectedGoalEvaluationHourType))

    rows = [...rows, this.getGoalsTimeAsRow(this.goalTimesEvaluation, this.selectedGoalEvaluationHourType)]
    this.data$.next(rows);
    this.columns$.next(this.dateService.getMonths(["Name"]));
  }

  loadValues() {
    this.columnFixedWidthFromIndex$.next(1);
    this.boldColumnIndices$.next([0])
  }

  getGoalsTimeAsRow(goalsTime: GoalsTimeEvaluationDto, type: EAssistancePlanEvaluationType | null): string[] {
    let cells = [this.truncateString("Hilfeplan", 20)];

    for (let i = 0; i < goalsTime.approvedHours.length; i++) {
      switch (type) {
        case EAssistancePlanEvaluationType.Approved:
          if (goalsTime.approvedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalsTime.approvedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedApproved:
          if (goalsTime.summedApprovedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalsTime.summedApprovedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Executed:
          if (goalsTime.executedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalsTime.executedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedExecuted:
          if (goalsTime.summedExecutedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalsTime.summedExecutedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Left:
          if (goalsTime.approvedHoursLeft[i] <= 0)
            cells.push("-")
          else
            cells.push((goalsTime.approvedHoursLeft[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedLeft:
          if (goalsTime.summedApprovedHoursLeft[i] <= 0)
            cells.push("-")
          else
            cells.push((goalsTime.summedApprovedHoursLeft[i]).toFixed(2).toString());
          break;
        default:
          break;
      }
    }
    return cells;
  }

  getGoalTimesAsRow(goalTimes: GoalTimeEvaluationDto, type: EAssistancePlanEvaluationType | null): string[] {
    let cells = [this.truncateString("Ziel: " + goalTimes.title, 20)];

    for (let i = 0; i < goalTimes.approvedHours.length; i++) {
      switch (type) {
        case EAssistancePlanEvaluationType.Approved:
          if (goalTimes.approvedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.approvedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedApproved:
          if (goalTimes.summedApprovedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.summedApprovedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Executed:
          if (goalTimes.executedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.executedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedExecuted:
          if (goalTimes.summedExecutedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.summedExecutedHours[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.Left:
          if (goalTimes.approvedHoursLeft[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.approvedHoursLeft[i]).toFixed(2).toString());
          break;
        case EAssistancePlanEvaluationType.SummedLeft:
          if (goalTimes.summedApprovedHoursLeft[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.summedApprovedHoursLeft[i]).toFixed(2).toString());
          break;
        default:
          break;
      }
    }
    return cells;
  }

  getTableButtonsAsRow(goalEvaluationYear: GoalEvaluationYearDto, year: number): TableButtonCell[] {
    let cells: TableButtonCell[] = [
      new TableButtonCell(false, false,false, this.truncateString(goalEvaluationYear.title, 120), 0)
    ]

    for (let i = 0; i < goalEvaluationYear.months.length; i++) {
      let exists = goalEvaluationYear.months[i].evaluation != null
      let checked = goalEvaluationYear.months[i].evaluation?.approved ?? false
      let enabled = goalEvaluationYear.months[i].assistancePlanActive
      let evaluation = goalEvaluationYear.months[i].evaluation
      let date = new Date(year, i, 1)
      cells.push(new TableButtonCell(exists, checked, enabled, "", { goalId: goalEvaluationYear.goalId, date: date, evaluation: evaluation}));
    }
    return cells;
  }

  truncateString(inputString: string, maxLength: number): string {
    if (inputString.length <= maxLength) {
      return inputString;
    } else {
      return inputString.substring(0, maxLength) + "...";
    }
  }

  openEvaluationModal(payload: { goalId: number, date: Date, evaluation: EvaluationDto }) {
    let dialogRef = this.dialog.open(AssistancePlanEvaluationModalComponent);
    let dialog = dialogRef.componentInstance;
    dialog.evaluation$.next(payload.evaluation);
    dialog.goalId$.next(payload.goalId);
    dialog.date$.next(payload.date);
    dialogRef.afterClosed().subscribe({
      next: _ => this.loadEvaluations()
    })
  }

  executeURLParams() {
    this.route.params.subscribe(params => {
      // tab
      if (params[this.tabParamName]) {
        const urlTabIndex = +params[this.tabParamName]
        if (this.validTabIndices.includes(urlTabIndex, 0)) {
          this.tabIndex = urlTabIndex;
        }
      }

      // goalId
      if (params[this.idParamName]) {
        this.assistancePlanId = +params[this.idParamName];
      }
    });
  };

  updateUrl() {
    this.location.go(`assistance_plans/goals/${this.assistancePlanId}/${this.tabIndex}`);
  }

  protected readonly console = console;
}
