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
import {NewPageComponent} from '../../../shared/components/new-page.component';
import {AssistancePlanDto} from '../../../shared/dtos/assistance-plan-dto.model';
import {
  AssistancePlanCreateDto,
  AssistancePlanCreateHourDto
} from '../../../shared/dtos/assistance-plan-create-dto.model';
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
import {AssistancePlanInfoForm} from '../../../shared/components/assistance-plans/components/assistance-plan-info-form';

@Component({
  selector: 'app-assistance-plan-new-single-page',
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
export class AssistancePlanNewSinglePageComponent extends NewPageComponent<AssistancePlanDto> implements OnInit {
  client: ClientDto = new ClientDto();
  institutions: InstitutionDto[] = [];
  sponsors: SponsorDto[] = [];
  affiliatedInstitutions: InstitutionDto[] = [];
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
    private converter: Converter
  ) {
    super(helperService, location);
  }

  override ngOnInit() {
    super.ngOnInit();
    this.initFormSubscriptions();
  }

  get canSave(): boolean {
    return this.generalForm.valid && !this.isSubmitting;
  }

  get isHoursSectionActive(): boolean {
    return true;
  }

  get isGoalsSectionActive(): boolean {
    return true;
  }

  get hasPlanHours(): boolean {
    return this.createValue.hours.length > 0;
  }

  get hasGoalHours(): boolean {
    return this.createValue.goals.some(goal => (goal.hours?.length ?? 0) > 0);
  }

  get canAddPlanHours(): boolean {
    return !this.hasGoalHours;
  }

  get canAddGoalHours(): boolean {
    return !this.hasPlanHours;
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
        }
      },
      error: () => this.handleFailure('Fehler beim laden des Klienten', true)
    });
  }

  initFormSubscriptions() {
    this.generalForm.start.valueChanges.subscribe(value => {
      const formatted = this.converter.formatDate(new Date(value));
      this.value.start = formatted;
      this.createValue.start = formatted;
    });
    this.generalForm.end.valueChanges.subscribe(value => {
      const formatted = this.converter.formatDate(new Date(value));
      this.value.end = formatted;
      this.createValue.end = formatted;
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
    this.createValue.hours = [...hours];
  }

  onGoalsChange(goals: AssistancePlanCreateDto['goals']) {
    this.createValue.goals = [...goals];
  }

  private syncCreateDtoFromForm() {
    this.createValue.start = this.value.start;
    this.createValue.end = this.value.end;
    this.createValue.clientId = this.client.id;
    this.createValue.institutionId = this.value.institutionId;
    this.createValue.sponsorId = this.value.sponsorId;
  }
}
