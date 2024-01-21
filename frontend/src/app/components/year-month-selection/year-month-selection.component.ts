import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import { PeriodMode } from './PeriodMode';
import {Period} from "./Period";
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-year-month-selection',
  templateUrl: './year-month-selection.component.html',
  styleUrls: ['./year-month-selection.component.css']
})
export class YearMonthSelectionComponent implements OnInit {

  @Input() withoutMonth: boolean = false;
  @Input() disabled = false;
  @Input() disabled$: ReplaySubject<boolean> = new ReplaySubject<boolean>();

  @Output() periodModeChangedEvent: EventEmitter<PeriodMode> = new EventEmitter<PeriodMode>()
  @Output() periodChangedEvent: EventEmitter<Period> = new EventEmitter<Period>()

  selectedPeriodMode: PeriodMode = PeriodMode.PERIOD_MODE_YEARLY;
  year: number = new Date(Date.now()).getFullYear();
  month: number = 0;

  selectionForm: FormGroup = new FormGroup({
    periodModeControl: new FormControl({
      value: PeriodMode.PERIOD_MODE_YEARLY,
      disabled: this.disabled
    }),
  });

  constructor() { }

  get periodModeControl() { return this.selectionForm.controls['periodModeControl']; }

  ngOnInit(): void {
    this.initInputSubscriptions()
    this.initFormControlSubscriptions()
  }

  initInputSubscriptions() {
    this.disabled$.subscribe({
      next: value => {
        this.disabled = value
      }
    })
  }

  initFormControlSubscriptions() {
    this.periodModeControl.valueChanges.subscribe(value => {
      if (this.month == 0) {
        this.month = new Date().getMonth() + 1;
      }
      // reset month
      if (value == PeriodMode.PERIOD_MODE_YEARLY || this.withoutMonth) {
        this.month = 0;
      }

      this.selectedPeriodMode = Number(value);
      this.periodModeChangedEvent.emit(this.selectedPeriodMode);
      this.emitPeriodChanged()
    });
    this.periodModeControl.setValue(PeriodMode.PERIOD_MODE_YEARLY.valueOf().toString());
  }

  nextYear() {
    this.year += 1;
    this.emitPeriodChanged();
  }

  prevYear() {
    this.year -= 1;
    this.emitPeriodChanged();
  }

  nextMonth() {
    if (this.month == 12) {
      this.month = 1;
      this.year += 1;
    } else {
      this.month += 1;
    }
    this.emitPeriodChanged();
  }

  prevMonth() {
    if (this.month == 1) {
      this.month = 12;
      this.year -= 1;
    } else {
      this.month -= 1;
    }
    this.emitPeriodChanged();
  }

  getMonthName(month: number): string {
    const date = new Date();
    date.setMonth(month - 1);
    return date.toLocaleString('de-DE', { month: 'long' });
  }

  emitPeriodChanged() {
    this.periodChangedEvent.emit(new Period(this.selectedPeriodMode, this.year, this.month));
  }

  protected readonly PeriodMode = PeriodMode;
}
