import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {Converter} from "../../shared/services/converter.helper";
import {ReadableInstitutionDto} from "../../shared/dtos/institution-readable-dto.model";
import {ServiceService} from "../../shared/services/service.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DateService} from "../../shared/services/date.service";
import {Service} from "../../shared/dtos/service.projection";
import {UserService} from "../../shared/services/user.service";
import {finalize, ReplaySubject, switchMap, take} from "rxjs";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";

@Component({
    selector: 'app-my-my-services',
    templateUrl: './my-services.component.html',
    styleUrls: ['./my-services.component.css'],
    standalone: false
})
export class MyServicesComponent implements OnInit {

  baseUrl: string = "/services/my";
  private readonly destroyRef = inject(DestroyRef);

  filterDateStart: Date = new Date(Date.now());
  title: String = "Meine";

  services: Service[] = [];
  selectedInstitution: ReadableInstitutionDto | undefined = undefined;
  start: Date = new Date(Date.now());
  end: Date = new Date(Date.now());
  searchString: string = "";
  isBusy = false;
  illegalMode = false;

  userId$: ReplaySubject<number> = new ReplaySubject<number>();

  constructor(
    private serviceService: ServiceService,
    private converter: Converter,
    private dateService: DateService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router) {
  }

  getDate(date: Date): string {
    return this.converter.formatDate(new Date(date));
  }

  ngOnInit(): void {
    this.userService.user$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => this.userId$.next(value.id));
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
      const start = params.get('start');
      const end = params.get('end');

      if (start === null || end === null) {
        this.navigate(this.start, this.end);
        return;
      }

      this.start = this.dateService.convertDEDateStringToDate(start ?? "") ?? new Date();
      this.end = this.dateService.convertDEDateStringToDate(end ?? "") ?? new Date();

      this.loadServices();
    });
  }

  onDateChanged(value: { start: Date, end: Date }) {
    this.start = value.start;
    this.end = value.end;

    this.navigate(this.start, this.end);
    this.loadServices();
  }

  onSearchStringChanged(searchString: string) {
    this.searchString = searchString;
  }

  loadServices() {
    this.isBusy = true;
    this.illegalMode = false;
    this.userId$
      .pipe(
        take(1),
        switchMap((employeeId: number) =>
          this.serviceService.getByEmployeeAndStartAndEnd(employeeId, this.start, this.end)
        ),
        finalize(() => {
          this.isBusy = false;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (services: Service[]) => {
          this.services = services;
        },
        error: () => {
          this.isBusy = false;
        }
      });
  }

  loadIllegalServices() {
    this.isBusy = true;
    this.illegalMode = true;
    this.userId$
      .pipe(
        take(1),
        switchMap((employeeId: number) => this.serviceService.getIllegalByEmployeeId(employeeId)),
        finalize(() => {
          this.isBusy = false;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: illegalServices => {
          this.services = illegalServices;
        },
        error: () => {
          this.isBusy = false;
        }
      });
  }

  private navigate(start: Date, end: Date) {
    void this.router.navigate([
      this.baseUrl,
      this.dateService.formatDateToYearMonthDay(start),
      this.dateService.formatDateToYearMonthDay(end)
    ]).catch(error => {
      console.error('Failed to navigate to my services', error);
    });
  }
}
