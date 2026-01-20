import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {Period} from "../../../../shared/components/year-month-selection/Period";
import {EAssistancePlanEvaluationType} from "./EAssistancePlanEvaluationType";
import {MatSelectChange} from "@angular/material/select";
import {HourTypeService} from "../../../../shared/services/hour-type.service";
import {HourTypeDto} from "../../../../shared/dtos/hour-type-dto.model";
import {ReplaySubject} from "rxjs";

@Component({
    selector: 'app-assistance-plan-time-evaluation-filter',
    templateUrl: './assistance-plan-time-evaluation-filter.component.html',
    styleUrls: ['./assistance-plan-time-evaluation-filter.component.css'],
    standalone: false
})
export class AssistancePlanTimeEvaluationFilterComponent implements OnInit {

  @Input() disabled = false;
  @Input() disabled$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

  @Output() onDateSelectionChanged: EventEmitter<Period> = new EventEmitter<Period>();
  @Output() onHourTypeSelectionChanged: EventEmitter<number> = new EventEmitter<number>();
  @Output() onEvaluationTypeSelectionChanged: EventEmitter<EAssistancePlanEvaluationType> = new EventEmitter<EAssistancePlanEvaluationType>();

  evaluationTypes: [string, EAssistancePlanEvaluationType][] = this.getGoalEvaluationHourTypes()
  selectedEvaluationType: EAssistancePlanEvaluationType = EAssistancePlanEvaluationType.Executed;
  hourTypes: HourTypeDto[] = []
  selectedHourType: string = "";

  filterGroup: UntypedFormGroup = new UntypedFormGroup({
    evaluationTypeControl: new UntypedFormControl({disabled: this.disabled}),
    hourTypeControl: new UntypedFormControl({disabled: this.disabled})
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

  getGoalEvaluationHourTypes(): [string, EAssistancePlanEvaluationType][] {
    let result: [string, number][] = []

    Object.values(EAssistancePlanEvaluationType).forEach((value) => {
      switch (value) {
        case EAssistancePlanEvaluationType.Approved:
          result.push(["Genehmigt", EAssistancePlanEvaluationType.Approved])
          break;
        case EAssistancePlanEvaluationType.SummedApproved:
          result.push(["Genehmigt summiert", EAssistancePlanEvaluationType.SummedApproved])
          break;
        case EAssistancePlanEvaluationType.Executed:
          result.push(["Geleistet", EAssistancePlanEvaluationType.Executed])
          break;
        case EAssistancePlanEvaluationType.SummedExecuted:
          result.push(["Geleistet summiert", EAssistancePlanEvaluationType.SummedExecuted])
          break;
        case EAssistancePlanEvaluationType.Left:
          result.push(["Genehmigt übrig", EAssistancePlanEvaluationType.Left])
          break;
        case EAssistancePlanEvaluationType.SummedLeft:
          result.push(["Genehmigt übrig summiert", EAssistancePlanEvaluationType.SummedLeft])
          break;
        default:
          break;
      }
    })

    return result;
  }
}
