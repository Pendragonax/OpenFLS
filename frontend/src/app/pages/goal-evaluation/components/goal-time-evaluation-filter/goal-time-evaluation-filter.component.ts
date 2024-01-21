import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {Period} from "../../../../components/year-month-selection/Period";
import {EGoalEvaluationType} from "./EGoalEvaluationType";
import {MatSelectChange} from "@angular/material/select";
import {HourTypeService} from "../../../../services/hour-type.service";
import {HourTypeDto} from "../../../../dtos/hour-type-dto.model";
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-goal-time-evaluation-filter',
  templateUrl: './goal-time-evaluation-filter.component.html',
  styleUrls: ['./goal-time-evaluation-filter.component.css']
})
export class GoalTimeEvaluationFilterComponent implements OnInit {

  @Input() disabled = false;
  @Input() disabled$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

  @Output() onDateSelectionChanged: EventEmitter<Period> = new EventEmitter<Period>();
  @Output() onHourTypeSelectionChanged: EventEmitter<number> = new EventEmitter<number>();
  @Output() onEvaluationTypeSelectionChanged: EventEmitter<EGoalEvaluationType> = new EventEmitter<EGoalEvaluationType>();

  evaluationTypes: [string, EGoalEvaluationType][] = this.getGoalEvaluationHourTypes()
  selectedEvaluationType: EGoalEvaluationType = EGoalEvaluationType.Executed;
  hourTypes: HourTypeDto[] = []
  selectedHourType: string = "";

  filterGroup: FormGroup = new FormGroup({
    evaluationTypeControl: new FormControl({disabled: this.disabled}),
    hourTypeControl: new FormControl({disabled: this.disabled})
  });

  get evaluationTypeControl() { return this.filterGroup.controls['evaluationTypeControl']; }

  get hourTypeControl() { return this.filterGroup.controls['hourTypeControl']; }

  constructor(
    private hourTypeService: HourTypeService
  ) { }

  ngOnInit(): void {
    this.loadValues()
    this.initInputSubscriptions()
    this.initFormControlSubscriptions()
  }

  loadValues() {
    this.hourTypeService.allValues$.subscribe({
      next: value => this.hourTypes = value
    })
  }

  initInputSubscriptions() {
    this.disabled$.subscribe({
      next: value => {
        if (value) {
          this.evaluationTypeControl.disable()
          this.hourTypeControl.disable()
        } else {
          this.evaluationTypeControl.enable()
          this.hourTypeControl.enable()
        }
      }
    })
  }

  initFormControlSubscriptions() {
    this.evaluationTypeControl.valueChanges.subscribe(value => {
      this.selectedEvaluationType = value;
    });
    this.hourTypeControl.valueChanges.subscribe(value => {
      this.selectedHourType = value;
    });
  }

  dateSelectionChanged(event: Period) {
    this.onDateSelectionChanged.emit(event);
  }

  hourTypeSelectionChanged(event: MatSelectChange) {
    this.onHourTypeSelectionChanged.emit(event.value);
  }

  evaluationTypeSelectionChanged(event: MatSelectChange) {
    this.onEvaluationTypeSelectionChanged.emit(event.value);
  }

  getGoalEvaluationHourTypes(): [string, EGoalEvaluationType][] {
    let result: [string, number][] = []

    Object.values(EGoalEvaluationType).forEach((value) => {
      switch (value) {
        case EGoalEvaluationType.Approved:
          result.push(["Genehmigt", EGoalEvaluationType.Approved])
          break;
        case EGoalEvaluationType.SummedApproved:
          result.push(["Genehmigt summiert", EGoalEvaluationType.SummedApproved])
          break;
        case EGoalEvaluationType.Executed:
          result.push(["Geleistet", EGoalEvaluationType.Executed])
          break;
        case EGoalEvaluationType.SummedExecuted:
          result.push(["Geleistet summiert", EGoalEvaluationType.SummedExecuted])
          break;
        case EGoalEvaluationType.Left:
          result.push(["Genehmigt übrig", EGoalEvaluationType.Left])
          break;
        case EGoalEvaluationType.SummedLeft:
          result.push(["Genehmigt übrig summiert", EGoalEvaluationType.SummedLeft])
          break;
        default:
          break;
      }
    })

    return result;
  }
}
