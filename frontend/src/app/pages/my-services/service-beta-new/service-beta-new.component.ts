import {Component, DestroyRef, inject} from '@angular/core';
import {UntypedFormControl, Validators} from "@angular/forms";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from "@angular/material/core";
import {ServiceDetailComponent} from "../../service-detail/service-detail.component";
import {UserService} from "../../../shared/services/user.service";
import {InstitutionService} from "../../../shared/services/institution.service";
import {ClientsService} from "../../../shared/services/clients.service";
import {HourTypeService} from "../../../shared/services/hour-type.service";
import {CategoriesService} from "../../../shared/services/categories.service";
import {SponsorService} from "../../../shared/services/sponsor.service";
import {ServiceService} from "../../../shared/services/service.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Converter} from "../../../shared/services/converter.helper";
import {HelperService} from "../../../shared/services/helper.service";
import {Location} from "@angular/common";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {ClientDto} from "../../../shared/dtos/client-dto.model";

@Component({
  selector: 'app-service-beta-new',
  templateUrl: './service-beta-new.component.html',
  styleUrls: ['./service-beta-new.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS },
  ],
  standalone: false
})
export class ServiceBetaNewComponent extends ServiceDetailComponent {
  private readonly betaDestroyRef = inject(DestroyRef);
  private lastServiceDate = '';
  useDuration = false;
  constructor(
    userService: UserService,
    institutionService: InstitutionService,
    clientService: ClientsService,
    hourTypeService: HourTypeService,
    categoryService: CategoriesService,
    sponsorService: SponsorService,
    serviceService: ServiceService,
    router: Router,
    route: ActivatedRoute,
    converter: Converter,
    helperService: HelperService,
    location: Location
  ) {
    super(
      userService,
      institutionService,
      clientService,
      hourTypeService,
      categoryService,
      sponsorService,
      serviceService,
      router,
      route,
      converter,
      helperService,
      location
    );

    this.firstForm.addControl(
      'durationMinutes',
      new UntypedFormControl(0, Validators.compose([Validators.min(0), Validators.max(1439)]))
    );
  }

  displayClient = (client: ClientDto | string | null): string => {
    if (!client || typeof client === 'string') {
      return client ?? '';
    }
    return `${client.lastName} ${client.firstName}`;
  };

  onClientSelected(client: ClientDto) {
    if (!client) {
      return;
    }
    this.clientControl.setValue(client);
    this.clientsControl.setValue(client.id);
  }

  get durationControl() { return this.firstForm.controls['durationMinutes']; }

  get isTimeValid(): boolean {
    const startHour = Number(this.startHourControl.value ?? 0);
    const startMinute = Number(this.startMinuteControl.value ?? 0);
    const endHour = Number(this.endHourControl.value ?? 0);
    const endMinute = Number(this.endMinuteControl.value ?? 0);
    const startTotal = startHour * 60 + startMinute;
    const endTotal = endHour * 60 + endMinute;
    return endTotal > startTotal;
  }

  get canSave(): boolean {
    return this.isTimeValid &&
      this.clientSelected &&
      !!this.institutionControl.value &&
      !!this.hourTypeControl.value &&
      this.assistancePlanSelected;
  }

  get selectedServiceDateDisplay(): string {
    return this.converter.formatDateToGerman(new Date(this.selectedServiceDate.toString()));
  }

  toggleDuration(useDuration: boolean) {
    this.useDuration = useDuration;
    if (this.useDuration) {
      this.updateEndFromDuration();
    }
  }

  private updateEndFromDuration() {
    if (!this.useDuration) {
      return;
    }

    const hour = Number(this.startHourControl.value ?? 0);
    const minute = Number(this.startMinuteControl.value ?? 0);
    const duration = Number(this.durationControl.value ?? 0);
    const safeDuration = Number.isFinite(duration) ? Math.max(0, Math.min(1439, Math.floor(duration))) : 0;
    const startTotal = hour * 60 + minute;
    const totalMinutes = startTotal + safeDuration;
    const clampedTotal = Math.max(0, Math.min(1439, totalMinutes));
    const nextHour = Math.floor(clampedTotal / 60);
    const nextMinute = clampedTotal % 60;
    this.endHourControl.setValue(nextHour);
    this.endMinuteControl.setValue(nextMinute);
  }

  private normalizeTime(hour: number, minute: number) {
    let nextHour = hour;
    let nextMinute = minute;

    if (nextMinute < 0) {
      nextHour -= 1;
      nextMinute = 59;
    }
    if (nextMinute > 59) {
      nextHour += 1;
      nextMinute = 0;
    }

    if (nextHour < 0) {
      nextHour = 0;
      nextMinute = 59;
    }
    if (nextHour > 23) {
      nextHour = 23;
      nextMinute = 0;
    }

    return { hour: nextHour, minute: nextMinute };
  }

  adjustStartMinute(delta: number) {
    const hour = Number(this.startHourControl.value ?? 0);
    const minute = Number(this.startMinuteControl.value ?? 0) + delta;
    const normalized = this.normalizeTime(hour, minute);
    this.startHourControl.setValue(normalized.hour);
    this.startMinuteControl.setValue(normalized.minute);
    this.updateEndFromDuration();
  }

  adjustEndMinute(delta: number) {
    const hour = Number(this.endHourControl.value ?? 0);
    const minute = Number(this.endMinuteControl.value ?? 0) + delta;
    const normalized = this.normalizeTime(hour, minute);
    this.endHourControl.setValue(normalized.hour);
    this.endMinuteControl.setValue(normalized.minute);
  }

  clampTime(kind: 'start' | 'end') {
    if (kind === 'start') {
      const hour = Number(this.startHourControl.value ?? 0);
      const minute = Number(this.startMinuteControl.value ?? 0);
      const normalized = this.normalizeTime(hour, minute);
      this.startHourControl.setValue(normalized.hour);
      this.startMinuteControl.setValue(normalized.minute);
      this.updateEndFromDuration();
    } else {
      const hour = Number(this.endHourControl.value ?? 0);
      const minute = Number(this.endMinuteControl.value ?? 0);
      const normalized = this.normalizeTime(hour, minute);
      this.endHourControl.setValue(normalized.hour);
      this.endMinuteControl.setValue(normalized.minute);
    }
  }

  clampDuration() {
    const duration = Number(this.durationControl.value ?? 0);
    const safeDuration = Number.isFinite(duration) ? Math.max(0, Math.min(1439, Math.floor(duration))) : 0;
    this.durationControl.setValue(safeDuration);
    this.updateEndFromDuration();
  }

  removeGoal(goalId: number) {
    const remainingIds = this.selectedGoals
      .filter(goal => goal.id !== goalId)
      .map(goal => goal.id);
    this.goalsControl.setValue(remainingIds);
  }

  removeCategory(categoryId: number) {
    const remainingIds = this.selectedCategories
      .filter(category => category.id !== categoryId)
      .map(category => category.id);
    this.categoriesControl.setValue(remainingIds);
  }

  override ngOnInit() {
    super.ngOnInit();
    const initialDate = this.serviceDateControl.value;
    this.lastServiceDate = initialDate ? new Date(initialDate.toString()).toDateString() : '';

    this.serviceDateControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe((value) => {
        if (!value) {
          return;
        }
        const nextDate = new Date(value.toString()).toDateString();
        if (nextDate === this.lastServiceDate) {
          return;
        }
        this.lastServiceDate = nextDate;
        this.assistancePlansControl.setValue(null);
        this.resetAssistancePlan();
        this.goalsControl.setValue([]);
        this.resetGoals();
        if (this.value.clientId) {
          this.loadClient(this.value.clientId);
        }
      });
  }

  override initFormSubscriptions() {
    if (!this.editMode) {
      this.clientControl.enable();
      this.clientControl.valueChanges
        .pipe(takeUntilDestroyed(this.betaDestroyRef))
        .subscribe((value: ClientDto | string | null) => {
          const isString = typeof value === 'string';
          const search = isString ? value : (value ? `${value.firstName} ${value.lastName}` : '');
          this.filteredClients = this._getClients(search || '');

          if (isString) {
            this.clientsControl.setValue(null);
            this.value.clientId = 0;
            this.clientSelected = false;
            this.filteredAssistancePlans = [];
            this.assistancePlansControl.setValue(null);
            this.resetAssistancePlan();
            this.goalsControl.setValue([]);
            this.resetGoals();
          }
        });

      this.clientsControl.enable();
      this.clientsControl.valueChanges
        .pipe(takeUntilDestroyed(this.betaDestroyRef))
        .subscribe((value: number | null) => {
          this.assistancePlansControl.setValue(null);
          this.resetAssistancePlan();
          this.goalsControl.setValue([]);
          this.resetGoals();
          this.resetCategories();
          this.categoriesControl.setValue([]);

          if (value != null) {
            this.value.clientId = value;
            this.clientSelected = true;
            this.loadClient(value);
          } else {
            this.value.clientId = 0;
            this.clientSelected = false;
            this.filteredAssistancePlans = [];
          }
        });
    }

    this.serviceDateControl.enable();
    this.serviceDateControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe((value) => {
        if (value != null) {
          this.selectedServiceDate = this.converter.formatDate(new Date(value.toString()));
          this.reloadTitle();
          this.minDate = new Date(value.toString());
        }
      });

    this.assistancePlansControl.enable();
    this.assistancePlansControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe((value: number | null) => {
        this.resetGoals();
        this.goalsControl.setValue([]);

        if (value != null) {
          this.selectAssistancePlan(this.selectedClient.assistancePlans.find(plan => plan.id == value));
        } else {
          this.selectAssistancePlan(null);
        }
      });

    this.startHourControl.enable();
    this.startHourControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => {
        if (value != null && !this.editMode) {
          if (this.useDuration) {
            this.updateEndFromDuration();
          } else {
            this.endHourControl.setValue(value);
          }
        }
      });

    this.startMinuteControl.enable();
    this.startMinuteControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => {
        if (value != null && !this.editMode) {
          if (this.useDuration) {
            this.updateEndFromDuration();
          } else {
            this.endMinuteControl.setValue(value);
          }
        }
      });

    this.endHourControl.enable();
    this.endMinuteControl.enable();

    this.durationControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(() => this.updateEndFromDuration());

    this.hourTypeControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => this.value.hourTypeId = value);

    this.institutionControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => this.value.institutionId = value);

    this.goalsControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe((value: number[]) => {
        if (value.length > 0) {
          this.setGoals(value);
        } else {
          this.resetGoals();
        }
      });

    this.categoriesControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe((value: number[]) => {
        if (value.length > 0) {
          this.setCategories(value);
        } else {
          this.resetCategories();
        }
      });

    this.contentControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => this.value.content = value);
    this.titleControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => this.value.title = value);
    this.unfinishedControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => this.value.unfinished = value);
    this.groupServiceControl.valueChanges
      .pipe(takeUntilDestroyed(this.betaDestroyRef))
      .subscribe(value => this.value.groupService = value);
  }

  resetAll() {
    this.firstForm.reset({
      serviceDate: this.timeNow,
      startHour: this.timeNow.getHours(),
      startMinute: this.timeNow.getMinutes(),
      endHour: this.timeNow.getHours(),
      endMinute: this.timeNow.getMinutes(),
      durationMinutes: 0,
      institution: null
    });

    this.secondForm.reset({
      client: '',
      clientList: null,
      assistancePlanList: null,
      goalList: [],
      hourType: null
    });

    this.thirdForm.reset({
      categoryList: [],
      title: '',
      content: '',
      unfinished: false,
      groupService: false
    });

    this.filteredAssistancePlans = [];
    this.selectedGoals = [];
    this.selectedCategories = [];
    this.clientSelected = false;
    this.assistancePlanSelected = false;
    this.useDuration = false;
  }
}
