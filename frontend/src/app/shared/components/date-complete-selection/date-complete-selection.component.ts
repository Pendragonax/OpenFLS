import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";

@Component({
    selector: 'app-date-complete-selection',
    imports: [
        FormsModule,
        MatButtonModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatIconModule,
        ReactiveFormsModule,
        MatTooltipModule
    ],
    templateUrl: './date-complete-selection.component.html',
    styleUrl: './date-complete-selection.component.css',
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
        {
            provide: DateAdapter,
            useClass: MomentDateAdapter,
            deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS],
        },
        { provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS },
    ]
})
export class DateCompleteSelectionComponent {

  @Input() disabled: boolean = false
  @Input() start: Date = new Date();
  @Input() end: Date = new Date();

  @Output() dateChanged: EventEmitter<{ start: Date, end: Date }> = new EventEmitter<{ start: Date, end: Date }>()

  dateGroup: FormGroup;

  get startControl() {
    return this.dateGroup.controls['startControl'];
  }

  get endControl() {
    return this.dateGroup.controls['endControl'];
  }

  constructor(private fb: FormBuilder) {
    this.dateGroup = this.fb.group({
      startControl: new FormControl({value: this.start, disabled: this.disabled}),
      endControl: new FormControl({value: this.end, disabled: this.disabled})
    });
  }

  ngOnInit(): void {
    this.startControl.setValue(this.start)
    this.endControl.setValue(this.end)

    this.dateGroup.valueChanges.subscribe(value => {
      this.start = value.startControl;
      this.end = value.endControl;
    });
  }

  setDateToToday(event) {
    event.stopPropagation();

    const date = new Date(Date.now());

    this.startControl.setValue(date);
    this.endControl.setValue(date);

    this.dateChanged.emit({start: this.start, end: this.end});
  }

  increaseDate(days: number) {
    const date = new Date(this.start);
    date.setDate(date.getDate() + days);

    this.startControl.setValue(date);
    this.endControl.setValue(date);

    this.dateChanged.emit({start: this.start, end: this.end});
  }

  decreaseDate(days: number) {
    const date = new Date(this.start);
    date.setDate(date.getDate() - days);

    this.startControl.setValue(date);
    this.endControl.setValue(date);

    this.dateChanged.emit({start: this.start, end: this.end});
  }

  setStartAndEnd(start, end) {
    if (start.value == null || end.value == null || start.value === "" || end.value === "") {
      return
    }

    this.start = this.parseDEDateString(start.value) ?? new Date();
    this.end = this.parseDEDateString(end.value) ?? new Date();

    this.dateChanged.emit({start: this.start, end: this.end});
  }

  private parseDEDateString(dateString: string): Date | null {
    const dateParts = dateString.split('.');

    if (dateParts.length !== 3) {
      return null;
    }

    const [day, month, year] = dateParts.map(part => parseInt(part));
    const adjustedMonth = month - 1;

    if ([day, adjustedMonth, year].some(isNaN)) {
      return null;
    }

    const date = new Date(year, adjustedMonth, day);

    return date.getDate() === day &&
    date.getMonth() === adjustedMonth &&
    date.getFullYear() === year
      ? date
      : null;
  }
}
