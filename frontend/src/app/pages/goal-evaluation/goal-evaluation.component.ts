import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {AssistancePlanService} from "../../services/assistance-plan.service";
import {AssistancePlanResponseDto} from "../../dtos/assistance-plan-response-dto.model";
import {PeriodMode} from "../../components/year-month-selection/PeriodMode";
import {ReplaySubject} from "rxjs";
import {DateService} from "../../services/date.service";
import {Period} from "../../components/year-month-selection/Period";
import {GoalResponseDto} from "../../dtos/goal-response-dto.model";
import {TableButtonCell} from "../../components/table-button/TableButtonCell";
import {MatDialog} from "@angular/material/dialog";
import {GoalEvaluationModalComponent} from "../../modals/goal-evaluation-modal/goal-evaluation-modal.component";
import {Location} from "@angular/common";
import {GoalTimeEvaluationService} from "../../services/goal-time-evaluation.service";
import {GoalsTimeEvaluationDto} from "../../dtos/goals-time-evaluation-dto.model";
import {GoalTimeEvaluationDto} from "../../dtos/goal-time-evaluation-dto.model";
import {EGoalEvaluationType} from "./components/goal-time-evaluation-filter/EGoalEvaluationType";
import {EvaluationsService} from "../../services/evaluations.service";
import {GoalEvaluationYearDto} from "../../dtos/goal-evaluation-year-dto.model";
import {EvaluationDto} from "../../dtos/evaluation-dto.model";

@Component({
  selector: 'app-goal-evaluation',
  templateUrl: './goal-evaluation.component.html',
  styleUrls: ['./goal-evaluation.component.css']
})
export class GoalEvaluationComponent implements OnInit {

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
  assistancePlan: AssistancePlanResponseDto = new AssistancePlanResponseDto();
  evaluations: GoalEvaluationYearDto = new GoalEvaluationYearDto();
  goalTimesEvaluation: GoalsTimeEvaluationDto = new GoalsTimeEvaluationDto();
  selectedGoalEvaluationHourType: EGoalEvaluationType | null = null;
  selectedGoalTimeHourTypeId: number = 0
  selectedGoalTimeYear: number = 0
  selectedEvaluationYear: number = 0
  tabIndex = 0;

  // STATE
  isGenerating: boolean = false
  errorOccurred: boolean = true

  constructor(private assistancePlanService: AssistancePlanService,
              private goalTimeEvaluationService: GoalTimeEvaluationService,
              private evaluationService: EvaluationsService,
              private dateService: DateService,
              private dialog: MatDialog,
              private route: ActivatedRoute,
              private location: Location) {}

  ngOnInit(): void {
    this.executeURLParams();
    this.assistancePlanService.getTestById(this.assistancePlanId).subscribe({
      next: value => {
        this.assistancePlan = value;
        this.loadValues();
        this.onEvaluationPeriodChanged(new Period(PeriodMode.PERIOD_MODE_YEARLY, new Date().getFullYear(), 1));
      }
    });
  }

  onGoalTimeEvaluationTypeChanged(type: EGoalEvaluationType) {
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
    this.errorOccurred = false
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
          this.errorOccurred = true
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
    this.data$.next(rows);
    this.columns$.next(this.dateService.getMonths(["Name"]));
  }

  loadValues() {
    this.columnFixedWidthFromIndex$.next(1);
    this.boldColumnIndices$.next([0])
  }

  getGoalTimesAsRow(goalTimes: GoalTimeEvaluationDto, type: EGoalEvaluationType | null): string[] {
    let cells = [this.truncateString(goalTimes.title, 20)];

    for (let i = 0; i < goalTimes.approvedHours.length; i++) {
      switch (type) {
        case EGoalEvaluationType.Approved:
          if (goalTimes.approvedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.approvedHours[i]).toFixed(2).toString());
          break;
        case EGoalEvaluationType.SummedApproved:
          if (goalTimes.summedApprovedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.summedApprovedHours[i]).toFixed(2).toString());
          break;
        case EGoalEvaluationType.Executed:
          if (goalTimes.executedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.executedHours[i]).toFixed(2).toString());
          break;
        case EGoalEvaluationType.SummedExecuted:
          if (goalTimes.summedExecutedHours[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.summedExecutedHours[i]).toFixed(2).toString());
          break;
        case EGoalEvaluationType.Left:
          if (goalTimes.approvedHoursLeft[i] <= 0)
            cells.push("-")
          else
            cells.push((goalTimes.approvedHoursLeft[i]).toFixed(2).toString());
          break;
        case EGoalEvaluationType.SummedLeft:
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
    let dialogRef = this.dialog.open(GoalEvaluationModalComponent);
    let dialog = dialogRef.componentInstance;
    dialog.evaluation$.next(payload.evaluation);
    dialog.goalId$.next(payload.goalId);
    dialog.date$.next(payload.date);
    dialogRef.afterClosed().subscribe({
      next: value => this.loadEvaluations()
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
