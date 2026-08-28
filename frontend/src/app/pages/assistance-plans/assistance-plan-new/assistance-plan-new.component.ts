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
import {Validators} from '@angular/forms';
import {combineLatest} from 'rxjs';
import {NewPageComponent} from '../../../shared/components/new-page.component';
import {AssistancePlanDto} from '../../../shared/dtos/assistance-plan-dto.model';
import {
  AssistancePlanCreateDto,
  AssistancePlanCreateHourDto
} from '../../../shared/dtos/assistance-plan-create-dto.model';
import {AssistancePlanHourMode} from '../../../shared/dtos/assistance-plan-hour-mode.model';
import {ClientDto} from '../../../shared/dtos/client-dto.model';
import {InstitutionDto} from '../../../shared/dtos/institution-dto.model';
import {SponsorDto} from '../../../shared/dtos/sponsor-dto.model';
import {AssistancePlanExistingDto} from '../../../shared/dtos/assistance-plan-existing-dto.model';
import {AssistancePlanService} from '../../../shared/services/assistance-plan.service';
import {ClientsService} from '../../../shared/services/clients.service';
import {Converter} from '../../../shared/services/converter.helper';
import {HelperService} from '../../../shared/services/helper.service';
import {InstitutionService} from '../../../shared/services/institution.service';
import {HourCorridorService} from '../../../shared/services/hour-corridor.service';
import {SponsorService} from '../../../shared/services/sponsor.service';
import {UserService} from '../../../shared/services/user.service';
import {AssistancePlanInfoForm} from '../components/assistance-plan-info-form/assistance-plan-info-form';
import {HourCorridorDto} from '../../../shared/dtos/hour-corridor-dto.model';

@Component({
  selector: 'app-assistance-plan-new-page',
  templateUrl: './assistance-plan-new.component.html',
  styleUrls: ['../../my-services/service-form.shared.css', './assistance-plan-new.component.css'],
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
export class AssistancePlanNewPageComponent extends NewPageComponent<AssistancePlanDto> implements OnInit {
  readonly AssistancePlanHourMode = AssistancePlanHourMode;

  client: ClientDto = new ClientDto();
  institutions: InstitutionDto[] = [];
  sponsors: SponsorDto[] = [];
  hourCorridors: HourCorridorDto[] = [];
  affiliatedInstitutions: InstitutionDto[] = [];
  existingPlans: AssistancePlanExistingDto[] = [];
  existingPlansLoading = false;
  createValue: AssistancePlanCreateDto = new AssistancePlanCreateDto();

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
    private hourCorridorService: HourCorridorService,
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
    return this.generalForm.valid && !this.hasEndDateError && !this.isSubmitting;
  }

  get isHoursSectionActive(): boolean {
    return true;
  }

  get isGoalsSectionActive(): boolean {
    return true;
  }

  get hasPlanHours(): boolean {
    return this.isExactMode && this.createValue.hours.length > 0;
  }

  get hasGoalHours(): boolean {
    return this.createValue.goals.some(goal => (goal.hours?.length ?? 0) > 0);
  }

  get canAddPlanHours(): boolean {
    return this.isExactMode && !this.hasGoalHours;
  }

  get canAddGoalHours(): boolean {
    return this.isExactMode && !this.hasPlanHours;
  }

  get isExactMode(): boolean {
    return this.generalForm.hourMode.value !== AssistancePlanHourMode.CORRIDOR;
  }

  get isCorridorMode(): boolean {
    return this.generalForm.hourMode.value === AssistancePlanHourMode.CORRIDOR;
  }

  get selectedHourMode(): AssistancePlanHourMode {
    return this.generalForm.hourMode.value ?? AssistancePlanHourMode.EXACT;
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
      this.hourCorridorService.allValues$,
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ]).subscribe(([institutions, sponsors, hourCorridors, affiliatedIds, isAdmin]) => {
      this.institutions = institutions;
      this.sponsors = sponsors;
      this.hourCorridors = hourCorridors;
      this.affiliatedInstitutions = this.institutions.filter(value =>
        isAdmin || affiliatedIds.some(id => id === value.id)
      );
      this.syncHourModeState(this.selectedHourMode, false);
    });

    this.loadClient();
  }

  loadClient() {
    const id = this.route.snapshot.paramMap.get('id') ?? 0;

    this.clientService.getById(+id).subscribe({
      next: (value) => {
        if (value == null) {
          this.handleFailure('Fehler beim laden des Klienten', true);
        } else {
          this.client = value;
          this.loadExistingPlans(value.id);
        }
      },
      error: () => this.handleFailure('Fehler beim laden des Klienten', true)
    });
  }

  initFormSubscriptions() {
    this.generalForm.start.valueChanges.subscribe(value => {
      const startDate = this.parseControlDate(value);

      if (startDate) {
        const formatted = this.converter.formatDate(startDate);
        this.value.start = formatted;
        this.createValue.start = formatted;
        this.generalForm.end.enable({emitEvent: false});
      } else {
        this.value.start = '';
        this.createValue.start = '';
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
        this.createValue.end = formatted;
      } else {
        this.value.end = '';
        this.createValue.end = '';
      }

      this.validateEndAfterStart();
    });
    this.generalForm.sponsor.valueChanges.subscribe(value => {
      const sponsorId = this.sponsors.find(sponsor => sponsor.id === value)?.id ?? 0;
      this.value.sponsorId = sponsorId;
      this.createValue.sponsorId = sponsorId;
    });
    this.generalForm.institution.valueChanges.subscribe(value => {
      const institutionId = this.institutions.find(inst => inst.id === value)?.id ?? 0;
      this.value.institutionId = institutionId;
      this.createValue.institutionId = institutionId;
    });
    this.generalForm.hourMode.valueChanges.subscribe(value => {
      this.syncHourModeState(value ?? AssistancePlanHourMode.EXACT, true);
    });
    this.generalForm.hourCorridor.valueChanges.subscribe(value => {
      this.createValue.hourCorridorId = Number(value ?? 0);
    });

    if (this.hasStartDate) {
      this.generalForm.end.enable({emitEvent: false});
    } else {
      this.generalForm.end.disable({emitEvent: false});
    }

    this.validateEndAfterStart();
  }

  create() {
    this.syncCreateDtoFromForm();
    this.createValue.clientId = this.client.id;

    this.assistancePlanService.create(this.createValue).subscribe({
      next: () => this.handleSuccess('Hilfeplan gespeichert', true),
      error: () => this.handleSuccess('Fehler beim speichern')
    });
  }

  onHoursChange(hours: AssistancePlanCreateHourDto[]) {
    this.createValue.hours = this.isCorridorMode ? [] : [...hours];
  }

  onGoalsChange(goals: AssistancePlanCreateDto['goals']) {
    this.createValue.goals = this.isCorridorMode
      ? goals.map(goal => ({...goal, hours: []}))
      : [...goals];
  }

  getHourModeLabel(mode: AssistancePlanHourMode | null | undefined): string {
    return mode === AssistancePlanHourMode.CORRIDOR ? 'Korridor' : 'Exakt';
  }

  getHourModeRange(corridorId: number): string {
    const corridor = this.hourCorridors.find(value => value.id === corridorId);
    if (!corridor) {
      return 'n/a';
    }

    return `${this.formatWeeklyMinutes(corridor.weeklyMinutesFrom)} - ${this.formatWeeklyMinutes(corridor.weeklyMinutesTill)}`;
  }

  getHourCorridorLabel(corridor: HourCorridorDto): string {
    return `${corridor.title} · ${this.formatWeeklyMinutes(corridor.weeklyMinutesFrom)} - ${this.formatWeeklyMinutes(corridor.weeklyMinutesTill)} · ${corridor.hourTypeTitle}`;
  }

  getExistingPlanTimeRange(plan: AssistancePlanExistingDto): string {
    return `${this.toGermanDate(plan.start)} - ${this.toGermanDate(plan.end)}`;
  }

  isExistingPlanInNewRange(plan: AssistancePlanExistingDto): boolean {
    const newStart = this.parseDate(this.createValue.start);
    const newEnd = this.parseDate(this.createValue.end);
    const planStart = this.parseDate(plan.start);
    const planEnd = this.parseDate(plan.end);

    if (!newStart || !newEnd || !planStart || !planEnd) {
      return false;
    }

    return newStart <= planEnd && newEnd >= planStart;
  }

  private loadExistingPlans(clientId: number) {
    this.existingPlansLoading = true;

    this.assistancePlanService.getExistingByClientId(clientId).subscribe({
      next: (plans) => {
        this.existingPlans = [...(plans ?? [])]
          .sort((a, b) => a.start.localeCompare(b.start));
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

  private syncHourModeState(mode: AssistancePlanHourMode, emitEvent: boolean) {
    this.createValue.hourMode = mode;

    if (mode === AssistancePlanHourMode.CORRIDOR) {
      this.createValue.hours = [];
      this.createValue.goals = this.createValue.goals.map(goal => ({
        ...goal,
        hours: []
      }));
      this.generalForm.hourCorridor.setValidators([Validators.required]);
    } else {
      this.createValue.hourCorridorId = 0;
      this.generalForm.hourCorridor.setValue(null, {emitEvent: false});
      this.generalForm.hourCorridor.clearValidators();
    }

    this.generalForm.hourCorridor.updateValueAndValidity({emitEvent});
  }

  private formatWeeklyMinutes(minutes: number): string {
    return (minutes / 60).toFixed(2).replace(/\.?0+$/, '');
  }

  private syncCreateDtoFromForm() {
    this.createValue.start = this.value.start;
    this.createValue.end = this.value.end;
    this.createValue.clientId = this.client.id;
    this.createValue.institutionId = this.value.institutionId;
    this.createValue.sponsorId = this.value.sponsorId;
  }
}
