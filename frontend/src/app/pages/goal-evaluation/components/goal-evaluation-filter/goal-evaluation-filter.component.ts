import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Period} from "../../../../shared/components/year-month-selection/Period";
import {UntypedFormGroup} from "@angular/forms";
import {PeriodMode} from "../../../../shared/components/year-month-selection/PeriodMode";

@Component({
  selector: 'app-goal-evaluation-filter',
  templateUrl: './goal-evaluation-filter.component.html',
  styleUrls: ['./goal-evaluation-filter.component.css']
})
export class GoalEvaluationFilterComponent implements OnInit {

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
