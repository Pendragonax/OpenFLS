import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
  MAT_NATIVE_DATE_FORMATS,
  NativeDateAdapter
} from '@angular/material/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest} from 'rxjs';
import {AssistancePlanInfoForm} from '../components/assistance-plan-info-form/assistance-plan-info-form';
import {NewPageComponent} from '../../../shared/components/new-page.component';
import {
  AssistancePlanCreateGoalDto,
  AssistancePlanCreateHourDto
} from '../../../shared/dtos/assistance-plan-create-dto.model';
import {AssistancePlanDto} from '../../../shared/dtos/assistance-plan-dto.model';
import {
  AssistancePlanUpdateDto,
  AssistancePlanUpdateGoalDto,
  AssistancePlanUpdateGoalHourDto,
  AssistancePlanUpdateHourDto
} from '../../../shared/dtos/assistance-plan-update-dto.model';
import {ClientDto} from '../../../shared/dtos/client-dto.model';
import {InstitutionDto} from '../../../shared/dtos/institution-dto.model';
import {SponsorDto} from '../../../shared/dtos/sponsor-dto.model';
import {AssistancePlanService} from '../../../shared/services/assistance-plan.service';
import {ClientsService} from '../../../shared/services/clients.service';
import {Converter} from '../../../shared/services/converter.helper';
import {HelperService} from '../../../shared/services/helper.service';
import {InstitutionService} from '../../../shared/services/institution.service';
import {SponsorService} from '../../../shared/services/sponsor.service';
import {UserService} from '../../../shared/services/user.service';

export function mapAssistancePlanDtoToUpdateDto(value: AssistancePlanDto): AssistancePlanUpdateDto {
  return {
    id: value.id,
    start: value.start,
    end: value.end,
    clientId: value.clientId,
    institutionId: value.institutionId,
    sponsorId: value.sponsorId,
    hours: (value.hours ?? []).map(hour => ({
      id: hour.id,
      assistancePlanId: hour.assistancePlanId,
      weeklyMinutes: hour.weeklyMinutes,
      hourTypeId: hour.hourTypeId
    })),
    goals: (value.goals ?? []).map(goal => ({
      id: goal.id,
      title: goal.title,
      description: goal.description,
      assistancePlanId: goal.assistancePlanId,
      institutionId: goal.institutionId,
      hours: (goal.hours ?? []).map(hour => ({
        id: hour.id,
        goalHourId: hour.goalHourId,
        weeklyMinutes: hour.weeklyMinutes,
        hourTypeId: hour.hourTypeId
      }))
    }))
  };
}

@Component({
  selector: 'app-assistance-plan-edit',
  templateUrl: './assistance-plan-edit.component.html',
  styleUrls: ['../../my-services/service-form.shared.css', './assistance-plan-edit.component.css'],
  providers: [
    {provide: MAT_DATE_LOCALE, useValue: 'de-DE'},
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE]
    },
    {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
  ],
  standalone: false
})
export class AssistancePlanEditComponent extends NewPageComponent<AssistancePlanDto> implements OnInit {
  client: ClientDto = new ClientDto();
  institutions: InstitutionDto[] = [];
  sponsors: SponsorDto[] = [];
  affiliatedInstitutions: InstitutionDto[] = [];
  existingPlans: AssistancePlanDto[] = [];
  existingPlansLoading = false;
  isLoading = true;
  planId = 0;
  updateValue: AssistancePlanUpdateDto = new AssistancePlanUpdateDto();

  private persistedPlanHourIds = new Set<number>();
  private persistedGoalIds = new Set<number>();
  private persistedGoalHourIdsByGoal = new Map<number, Set<number>>();

  generalForm = new AssistancePlanInfoForm();

  constructor(
    private institutionService: InstitutionService,
    private sponsorService: SponsorService,
    private assistancePlanService: AssistancePlanService,
    override helperService: HelperService,
    override location: Location,
    private route: ActivatedRoute,
    private clientService: ClientsService,
    private userService: UserService,
    private converter: Converter
  ) {
    super(helperService, location);
  }

  override ngOnInit() {
    super.ngOnInit();
    this.initFormSubscriptions();
    this.generalForm.start.setValue(null);
    this.generalForm.end.setValue(null);
  }

  get canSave(): boolean {
    return this.generalForm.valid && !this.hasEndDateError && !this.isSubmitting && !this.isLoading;
  }

  get hasPlanHours(): boolean {
    return this.updateValue.hours.length > 0;
  }

  get hasGoalHours(): boolean {
    return this.updateValue.goals.some(goal => (goal.hours?.length ?? 0) > 0);
  }

  get canAddPlanHours(): boolean {
    return !this.hasGoalHours;
  }

  get canAddGoalHours(): boolean {
    return !this.hasPlanHours;
  }

  get hasExistingPlans(): boolean {
    return this.existingPlans.length > 0;
  }

  get hasStartDate(): boolean {
    return this.parseControlDate(this.generalForm.start.value) != null;
  }

  get minEndDate(): Date | null {
    const startDate = this.parseControlDate(this.generalForm.start.value);
    if (!startDate) {
      return null;
    }

    const minDate = new Date(startDate);
    minDate.setDate(minDate.getDate() + 1);
    return minDate;
  }

  get hasEndDateError(): boolean {
    return this.generalForm.end.hasError('required') ||
      this.generalForm.end.hasError('matDatepickerParse') ||
      this.generalForm.end.hasError('endBeforeOrEqualStart');
  }

  getNewValue(): AssistancePlanDto {
    return new AssistancePlanDto();
  }

  loadReferenceValues() {
    combineLatest([
      this.institutionService.allValues$,
      this.sponsorService.allValues$,
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ]).subscribe(([institutions, sponsors, affiliatedIds, isAdmin]) => {
      this.institutions = institutions;
      this.sponsors = sponsors;
      this.affiliatedInstitutions = this.institutions.filter(value =>
        isAdmin || affiliatedIds.some(id => id === value.id)
      );
    });

    this.loadAssistancePlan();
  }

  initFormSubscriptions() {
    this.generalForm.start.valueChanges.subscribe(value => {
      const startDate = this.parseControlDate(value);

      if (startDate) {
        const formatted = this.converter.formatDate(startDate);
        this.value.start = formatted;
        this.updateValue.start = formatted;
        this.generalForm.end.enable({emitEvent: false});
      } else {
        this.value.start = '';
        this.updateValue.start = '';
        this.generalForm.end.setValue(null, {emitEvent: false});
        this.generalForm.end.disable({emitEvent: false});
      }

      this.validateEndAfterStart();
    });
    this.generalForm.end.valueChanges.subscribe(value => {
      const endDate = this.parseControlDate(value);
      if (endDate) {
        const formatted = this.converter.formatDate(endDate);
        this.value.end = formatted;
        this.updateValue.end = formatted;
      } else {
        this.value.end = '';
        this.updateValue.end = '';
      }

      this.validateEndAfterStart();
    });
    this.generalForm.sponsor.valueChanges.subscribe(value => {
      const sponsorId = this.sponsors.find(sponsor => sponsor.id === value)?.id ?? 0;
      this.value.sponsorId = sponsorId;
      this.updateValue.sponsorId = sponsorId;
    });
    this.generalForm.institution.valueChanges.subscribe(value => {
      const institutionId = this.institutions.find(inst => inst.id === value)?.id ?? 0;
      this.value.institutionId = institutionId;
      this.updateValue.institutionId = institutionId;
    });

    if (this.hasStartDate) {
      this.generalForm.end.enable({emitEvent: false});
    } else {
      this.generalForm.end.disable({emitEvent: false});
    }

    this.validateEndAfterStart();
  }

  update() {
    this.syncUpdateDtoFromForm();

    this.assistancePlanService.updateWithCreateLikeDto(this.planId, this.updateValue).subscribe({
      next: () => this.handleSuccess('Hilfeplan gespeichert', true),
      error: () => this.handleFailure('Fehler beim speichern')
    });
  }

  create() {
    this.update();
  }

  onHoursChange(hours: AssistancePlanCreateHourDto[]) {
    this.updateValue.hours = (hours ?? []).map((hour): AssistancePlanUpdateHourDto => ({
      id: this.persistedPlanHourIds.has(Number(hour.id ?? 0)) ? Number(hour.id) : 0,
      assistancePlanId: this.planId,
      weeklyMinutes: hour.weeklyMinutes,
      hourTypeId: hour.hourTypeId
    }));
  }

  onGoalsChange(goals: AssistancePlanCreateGoalDto[]) {
    this.updateValue.goals = (goals ?? []).map((goal): AssistancePlanUpdateGoalDto => {
      const persistedGoalId = this.persistedGoalIds.has(Number(goal.id ?? 0)) ? Number(goal.id) : 0;
      const persistedGoalHourIds = this.persistedGoalHourIdsByGoal.get(persistedGoalId) ?? new Set<number>();

      return {
        id: persistedGoalId,
        title: goal.title,
        description: goal.description,
        assistancePlanId: this.planId,
        institutionId: goal.institutionId,
        hours: (goal.hours ?? []).map((hour): AssistancePlanUpdateGoalHourDto => ({
          id: persistedGoalHourIds.has(Number(hour.id ?? 0)) ? Number(hour.id) : 0,
          goalHourId: persistedGoalId,
          weeklyMinutes: hour.weeklyMinutes,
          hourTypeId: hour.hourTypeId
        }))
      };
    });
  }

  getExistingPlanTimeRange(plan: AssistancePlanDto): string {
    return `${this.toGermanDate(plan.start)} - ${this.toGermanDate(plan.end)}`;
  }

  getExistingPlanSponsor(plan: AssistancePlanDto): string {
    return this.sponsors.find(sponsor => sponsor.id === plan.sponsorId)?.name ?? 'n/a';
  }

  isExistingPlanInNewRange(plan: AssistancePlanDto): boolean {
    const newStart = this.parseDate(this.updateValue.start);
    const newEnd = this.parseDate(this.updateValue.end);
    const planStart = this.parseDate(plan.start);
    const planEnd = this.parseDate(plan.end);

    if (!newStart || !newEnd || !planStart || !planEnd) {
      return false;
    }

    return newStart <= planEnd && newEnd >= planStart;
  }

  endDateFilter = (candidate: Date | null): boolean => {
    if (!candidate) {
      return false;
    }

    const minDate = this.minEndDate;
    if (!minDate) {
      return false;
    }

    return candidate >= minDate;
  };

  onEndDateBlur() {
    if (this.generalForm.end.hasError('matDatepickerParse')) {
      this.helperService.openSnackBar('Bitte ein gültiges Enddatum über den Kalender auswählen.');
      return;
    }

    if (this.generalForm.end.hasError('endBeforeOrEqualStart')) {
      this.helperService.openSnackBar('Das Enddatum muss nach dem Beginndatum liegen.');
    }
  }

  private loadAssistancePlan() {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.planId = Number(idParam ?? 0);

    if (!this.planId || this.planId <= 0) {
      this.handleFailure('Ungültige Hilfeplan-ID', true);
      return;
    }

    this.isLoading = true;
    this.assistancePlanService.getById(this.planId).subscribe({
      next: (plan) => {
        this.value = plan;
        this.updateValue = mapAssistancePlanDtoToUpdateDto(plan);
        this.rememberPersistedIds(plan);
        this.applyFormValuesFromPlan(plan);
        this.loadClient(plan.clientId);
      },
      error: () => this.handleFailure('Fehler beim Laden des Hilfeplans', true)
    });
  }

  private rememberPersistedIds(plan: AssistancePlanDto) {
    this.persistedPlanHourIds = new Set((plan.hours ?? []).map(hour => hour.id).filter(id => id > 0));
    this.persistedGoalIds = new Set((plan.goals ?? []).map(goal => goal.id).filter(id => id > 0));

    this.persistedGoalHourIdsByGoal = new Map<number, Set<number>>();
    (plan.goals ?? []).forEach(goal => {
      this.persistedGoalHourIdsByGoal.set(
        goal.id,
        new Set((goal.hours ?? []).map(hour => hour.id).filter(id => id > 0))
      );
    });
  }

  private applyFormValuesFromPlan(plan: AssistancePlanDto) {
    const startDate = this.parseDate(plan.start);
    const endDate = this.parseDate(plan.end);

    this.generalForm.start.setValue(startDate);
    this.generalForm.end.setValue(endDate);
    this.generalForm.sponsor.setValue(plan.sponsorId);
    this.generalForm.institution.setValue(plan.institutionId);
  }

  private loadClient(clientId: number) {
    this.clientService.getById(clientId).subscribe({
      next: (value) => {
        if (value == null) {
          this.handleFailure('Fehler beim Laden des Klienten', true);
          return;
        }

        this.client = value;
        this.loadExistingPlans(value.id);
        this.isLoading = false;
      },
      error: () => this.handleFailure('Fehler beim Laden des Klienten', true)
    });
  }

  private loadExistingPlans(clientId: number) {
    this.existingPlansLoading = true;

    this.assistancePlanService.getByClientId(clientId).subscribe({
      next: (plans) => {
        this.existingPlans = (plans ?? []).filter(plan => plan.id !== this.planId);
        this.existingPlansLoading = false;
      },
      error: () => {
        this.existingPlans = [];
        this.existingPlansLoading = false;
      }
    });
  }

  private toGermanDate(dateValue: string): string {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) {
      return dateValue;
    }
    return this.converter.formatDateToGerman(date);
  }

  private parseDate(dateValue: string): Date | null {
    const date = new Date(dateValue);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private validateEndAfterStart() {
    const startDate = this.parseControlDate(this.generalForm.start.value);
    const endDate = this.parseControlDate(this.generalForm.end.value);
    const currentErrors = {...(this.generalForm.end.errors ?? {})};

    delete currentErrors['endBeforeOrEqualStart'];

    if (startDate && endDate && endDate <= startDate) {
      this.generalForm.end.setErrors({...currentErrors, endBeforeOrEqualStart: true});
      return;
    }

    if (Object.keys(currentErrors).length > 0) {
      this.generalForm.end.setErrors(currentErrors);
      return;
    }

    this.generalForm.end.setErrors(null);
  }

  private parseControlDate(value: unknown): Date | null {
    if (value == null || value === '') {
      return null;
    }

    const date = value instanceof Date ? value : new Date(value as string | number);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private syncUpdateDtoFromForm() {
    this.updateValue.id = this.planId;
    this.updateValue.start = this.value.start;
    this.updateValue.end = this.value.end;
    this.updateValue.clientId = this.client.id;
    this.updateValue.institutionId = this.value.institutionId;
    this.updateValue.sponsorId = this.value.sponsorId;

    this.updateValue.hours = this.updateValue.hours.map(hour => ({
      ...hour,
      assistancePlanId: this.planId
    }));

    this.updateValue.goals = this.updateValue.goals.map(goal => ({
      ...goal,
      assistancePlanId: this.planId,
      hours: goal.hours.map(hour => ({
        ...hour,
        goalHourId: goal.id > 0 ? goal.id : 0
      }))
    }));
  }
}
