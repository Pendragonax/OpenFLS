import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {ReplaySubject, switchMap} from "rxjs";
import {EmployeeDto} from "../../../dtos/employee-dto.model";
import {ContingentDto} from "../../../dtos/contingent-dto.model";
import {Converter} from "../../converter.helper";
import {ServiceService} from "../../../services/service.service";
import {ServiceTimeDayDto} from "../../../dtos/service-time-day-dto.model";
import {MatCalendarCellCssClasses} from "@angular/material/datepicker";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-contingent-evaluation',
  templateUrl: './contingent-evaluation.component.html',
  styleUrls: ['./contingent-evaluation.component.css']
})
export class ContingentEvaluationComponent implements OnInit {
  @Input() employee$: ReplaySubject<EmployeeDto> = new ReplaySubject<EmployeeDto>();
  @Input() hideRefresh = false;
  @Output() onRefreshClick = new EventEmitter<any>();

  @ViewChild('serviceCalendar') serviceCalendar;

  // STATEs
  isSubmitting = false;

  // VARs
  employee: EmployeeDto = new EmployeeDto();
  time1: [number, number, number, number] = [0, 0, 0, 0];
  time7: [number, number, number, number] = [0, 0, 0, 0];
  time30: [number, number, number, number] = [0, 0, 0, 0];
  serviceDates: [Date, number][] = [];

  constructor(
    private serviceService: ServiceService,
    private userService: UserService,
    private converter: Converter
  ) { }

  ngOnInit(): void {
    this.loadValues();
  }

  loadValues() {
    this.isSubmitting = true;

    this.employee$
      .pipe(switchMap((value: EmployeeDto) => {
        this.employee = value;

        const past = new Date(Date.now());
        past.setDate(past.getDate() - 29);
        return this.serviceService.getTimesByEmployeeAndStartEnd(value.id, past, new Date(Date.now()))
      }))
      .subscribe({
        next: (value) => {
          const today = new Date(Date.now());

          this.time1[0] = this.converter.roundTo2Digits(
            this.findContingent(this.employee.contingents, today).weeklyServiceHours / 5);
          this.time1[1] = this.getServiceHours(value.days, 0);
          this.time1[2] = this.converter.roundTo2Digits(this.time1[1] - this.time1[0]);
          this.time1[3] = (this.time1[0] > 0)
            ? this.converter.roundTo2Digits(this.time1[1] * 100 / this.time1[0])
            : 100.0;

          this.time7[0] = this.converter.roundTo2Digits(this.getContingentHoursByPeriod(7, this.employee.contingents));
          this.time7[1] = this.getServiceHours(value.days, 6);
          this.time7[2] = this.converter.roundTo2Digits(this.time7[1] - this.time7[0]);
          this.time7[3] = (this.time7[0] > 0)
            ? this.converter.roundTo2Digits(this.time7[1] * 100 / this.time7[0])
            : 100.0;

          this.time30[0] = this.converter.roundTo2Digits(this.getContingentHoursByPeriod(30, this.employee.contingents));
          this.time30[1] = this.getServiceHours(value.days, 29);
          this.time30[2] = this.converter.roundTo2Digits(this.time30[1] - this.time30[0]);
          this.time30[3] = (this.time30[0] > 0)
            ? this.converter.roundTo2Digits(this.time30[1] * 100 / this.time30[0])
            : 100.0;

          this.serviceDates = this.getServiceDatesAndHours(value.days, this.employee.contingents);

          this.serviceCalendar.updateTodaysDate();
          this.isSubmitting = false;
        },
        error: () => this.isSubmitting = false
      });
  }

  refresh() {
    this.onRefreshClick.emit();
  }

  dateClass() {
    return (date: Date): MatCalendarCellCssClasses => {
      const serviceDate = this.serviceDates.find(x => x[0].toISOString() == date.toISOString());

      if (serviceDate != null) {
        return serviceDate[1] < 1 ? serviceDate[1] < 0.95 ? 'red-date' : 'yellow-date' : 'green-date';
      } else {
        return '';
      }
    };
  }

  private getServiceHours(days: ServiceTimeDayDto[], daysInPast: number): number {
    if (days.length <= 0)
      return 0;

    const begin = new Date(Date.now());
    begin.setDate(begin.getDate() - daysInPast);
    begin.setHours(0, 0, 0, 0);

    const timeValues = days
      .filter(time => (new Date(time.date)).toISOString() >= begin.toISOString())
      .map(time => time.hours);

    return (timeValues.length > 0)
      ? this.converter.roundTo2Digits(timeValues.reduce((sum, current) => sum + current) ?? 0.0)
      : 0;
  }

  private getContingentHoursByPeriod(days: number, contingents: ContingentDto[]) : number {
    let sum = 0;
    const date = new Date(Date.now());

    for (let i = 0; i < days; i++) {
      sum += this.getContingentHoursByDate(date, contingents);
      date.setDate(date.getDate() - 1);
    }

    return sum;
  }

  private getContingentHoursByDate(date: Date, contingents: ContingentDto[]) : number {
    const day = date.getDay();
    date.setHours(0, 0, 0, 0);
    return (day <= 0 || day >= 6)
      ? 0
      : this.converter.roundTo2Digits(this.findContingent(contingents, date).weeklyServiceHours / 5);
  }

  private getServiceDatesAndHours(days: ServiceTimeDayDto[], contingents: ContingentDto[]): [Date, number][] {
    return days.map(value => {
      const date = new Date(value.date);
      date.setHours(0, 0, 0, 0);
      return [
        date,
        this.converter.roundTo2Digits(
          value.hours / this.getContingentHoursByDate(date, contingents))];
    });
  }

  private findContingent(contingents: ContingentDto[], date: Date): ContingentDto {
    return contingents.find(value => {
      const start = new Date(value.start);
      start.setHours(0, 0, 0, 0);
      const end = value.end ? new Date(value.end) : null;
      end?.setHours(23,59,59);

      return start <= date && (end == null || end >= date);
    }) ?? new ContingentDto();
  }
}
