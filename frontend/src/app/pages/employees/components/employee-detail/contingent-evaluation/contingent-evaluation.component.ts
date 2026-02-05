import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {ReplaySubject, switchMap} from "rxjs";
import {EmployeeDto} from "../../../../../shared/dtos/employee-dto.model";
import {MatCalendarCellCssClasses} from "@angular/material/datepicker";
import {MatMenuTrigger} from "@angular/material/menu";
import {Router} from "@angular/router";
import {DateService} from "../../../../../shared/services/date.service";
import {CalendarInformationDTO} from "../../../../../shared/dtos/calendar-information-dto.model";
import {AbsenceService} from "../../../../../shared/services/absence.service";
import {UserService} from "../../../../../shared/services/user.service";
import {ContingentsService} from "../../../../../shared/services/contingents.service";

@Component({
    selector: 'app-contingent-evaluation',
    templateUrl: './contingent-evaluation.component.html',
    styleUrls: ['./contingent-evaluation.component.css'],
    standalone: false
})
export class ContingentEvaluationComponent implements OnInit {
  @Input() employee$: ReplaySubject<EmployeeDto> = new ReplaySubject<EmployeeDto>();
  @Input() hideRefresh = false;
  @Input() navigateToMyServices = true;
  @Output() onRefreshClick = new EventEmitter<any>();

  @ViewChild('serviceCalendar') serviceCalendar;
  @ViewChild('menuTrigger') menuTrigger!: MatMenuTrigger;

  // STATEs
  isSubmitting = false;

  // VARs
  employee: EmployeeDto = new EmployeeDto();
  calendarInformation: CalendarInformationDTO = new CalendarInformationDTO();
  serviceAllBaseUrl = "services/all"
  serviceMyBaseUrl = "services/my"
  menuPosition = { x: 0, y: 0 };
  lastSelectedDate: Date | null = null;
  userId: number = 0;

  constructor(
    private contingentService: ContingentsService,
    private router: Router,
    private dateService: DateService,
    private absenceService: AbsenceService,
    private userService: UserService,
  ) { }

  ngOnInit(): void {
    this.loadValues();
  }

  loadValues() {
    this.isSubmitting = true;

    this.userService.user$.subscribe(value => this.userId = value.id);
    this.employee$
      .pipe(switchMap((value: EmployeeDto) => {
        this.employee = value;
        return this.contingentService.getCalendarInformation(value.id, new Date(Date.now()))
      }))
      .subscribe({
        next: (value) => {
          this.calendarInformation = value;
          this.isSubmitting = false;
          this.serviceCalendar.updateTodaysDate();
        },
        error: () => this.isSubmitting = false
      });
  }

  refresh() {
    this.onRefreshClick.emit();
  }

  dateClass() {
    return (date: Date): MatCalendarCellCssClasses => {
      const serviceDate = this.calendarInformation.days.find(x => {
        return (new Date(x.date + 'T00:00:00').getTime() === date.getTime())
      })

      if (serviceDate != null) {
        if (serviceDate.absence) {
          return serviceDate.executedMinutes > 0 || serviceDate.executedHours > 0 ? 'error-date' : 'grey-date';
        }

        return serviceDate.executedPercentage < this.calendarInformation.today.warningPercent ? 'red-date' : serviceDate.executedPercentage < 100 ? 'yellow-date' : 'green-date';
      } else {
        return '';
      }
    };
  }

  onDateClicked(date) {
    this.lastSelectedDate = new Date(date);
    this.openMenu();
  }

  onCalendarMouseDown(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.menuPosition = { x: event.clientX, y: event.clientY };
  }

  onMenuOpened() {
    setTimeout(() => (document.activeElement as HTMLElement | null)?.blur());
  }

  onGoToDocumentation() {
    if (!this.lastSelectedDate) {
      return;
    }

    const formattedDate = this.dateService.formatDateToYearMonthDay(this.lastSelectedDate);

    if (!this.navigateToMyServices) {
      this.router.navigate([
        this.serviceAllBaseUrl,
        formattedDate,
        formattedDate,
        0,
        this.employee.id,
        0
      ]);
    } else {
      this.router.navigate([
        this.serviceMyBaseUrl,
        formattedDate,
        formattedDate
      ]);
    }
  }

  onMarkAbsent() {
    if (!this.lastSelectedDate) {
      return;
    }

    this.absenceService.create(this.userId, this.lastSelectedDate).subscribe(() => {
      this.loadValues();
    });
  }

  onRemoveAbsent() {
    if (!this.lastSelectedDate) {
      return;
    }

    this.absenceService.remove(this.lastSelectedDate).subscribe(() => {
      this.loadValues();
    });
  }

  private openMenu() {
    setTimeout(() => this.menuTrigger?.openMenu());
  }
}
