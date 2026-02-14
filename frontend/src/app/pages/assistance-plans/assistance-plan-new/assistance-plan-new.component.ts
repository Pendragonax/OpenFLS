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
import {combineLatest, ReplaySubject} from 'rxjs';
import {NewPageComponent} from '../../../shared/components/new-page.component';
import {AssistancePlanDto} from '../../../shared/dtos/assistance-plan-dto.model';
import {AssistancePlanHourDto} from '../../../shared/dtos/assistance-plan-hour-dto.model';
import {GoalDto} from '../../../shared/dtos/goal-dto.model';
import {ClientDto} from '../../../shared/dtos/client-dto.model';
import {InstitutionDto} from '../../../shared/dtos/institution-dto.model';
import {SponsorDto} from '../../../shared/dtos/sponsor-dto.model';
import {AssistancePlanView} from '../../../shared/models/assistance-plan-view.model';
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
  valueView$: ReplaySubject<AssistancePlanView> = new ReplaySubject<AssistancePlanView>();
  client: ClientDto = new ClientDto();
  institutions: InstitutionDto[] = [];
  sponsors: SponsorDto[] = [];
  affiliatedInstitutions: InstitutionDto[] = [];
  private localGoalId = -1;

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
      this.valueView$.next({
        dto: this.value,
        editable: true
      } as AssistancePlanView);

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
    this.generalForm.start.valueChanges.subscribe(value => this.value.start = this.converter.formatDate(new Date(value)));
    this.generalForm.end.valueChanges.subscribe(value => this.value.end = this.converter.formatDate(new Date(value)));
    this.generalForm.sponsor.valueChanges.subscribe(value =>
      this.value.sponsorId = this.sponsors.find(sponsor => sponsor.id === value)?.id ?? 0
    );
    this.generalForm.institution.valueChanges.subscribe(value =>
      this.value.institutionId = this.institutions.find(inst => inst.id === value)?.id ?? 0
    );
  }

  create() {
    this.value.clientId = this.client.id;

    this.assistancePlanService.create(this.value).subscribe({
      next: () => this.handleSuccess('Hilfeplan gespeichert', true),
      error: () => this.handleSuccess('Fehler beim speichern')
    });
  }

  addAssistancePlanHour(value: AssistancePlanHourDto) {
    this.value.hours.push(value);
  }

  updateAssistancePlanHour(value: AssistancePlanHourDto) {
    const index = this.value.hours.findIndex(x => x.id === value.id);

    if (index >= 0) {
      this.value.hours[index] = value;
    }
  }

  deleteAssistancePlanHour(value: AssistancePlanHourDto) {
    this.value.hours = this.value.hours.filter(x => x.id !== value.id);
  }

  addGoal(value: GoalDto) {
    const goal = {...value};
    if (goal.id <= 0) {
      goal.id = this.localGoalId--;
    }
    this.value.goals.push(goal);
  }

  updateGoal(value: GoalDto) {
    const index = this.value.goals.findIndex(goal => goal.id === value.id);
    if (index >= 0) {
      this.value.goals[index] = {...value};
    }
  }

  deleteGoal(value: GoalDto) {
    this.value.goals = this.value.goals.filter(goal => goal.id !== value.id);
  }
}
