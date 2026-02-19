import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Period} from "../../../../shared/components/year-month-selection/Period";
import {UntypedFormGroup} from "@angular/forms";

@Component({
    selector: 'app-assistance-plan-analysis-filter',
    templateUrl: './assistance-plan-evaluation-filter.component.html',
    styleUrls: ['./assistance-plan-evaluation-filter.component.css'],
    standalone: false
})
export class AssistancePlanEvaluationFilterComponent implements OnInit {

  @Output() onDateSelectionChanged: EventEmitter<Period> = new EventEmitter<Period>();

  disabled = false;

  filterGroup: UntypedFormGroup = new UntypedFormGroup({
  });

  constructor() { }

  ngOnInit(): void {
    this.initFormControlSubscriptions();
  }

  dateSelectionChanged(event: Period) {
    this.onDateSelectionChanged.emit(event);
  }

  initFormControlSubscriptions() {
  }

}
