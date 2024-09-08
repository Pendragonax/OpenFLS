import {Component, OnInit} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {InstitutionDto} from "../../../../dtos/institution-dto.model";
import {SponsorDto} from "../../../../dtos/sponsor-dto.model";
import {AssistancePlanService} from "../../../../services/assistance-plan.service";
import {ActivatedRoute} from "@angular/router";
import {SponsorService} from "../../../../services/sponsor.service";
import {combineLatest, ReplaySubject} from "rxjs";
import {AssistancePlanView} from "../../../../models/assistance-plan-view.model";
import {UserService} from "../../../../services/user.service";
import {Converter} from "../../../../services/converter.helper";
import {ClientsService} from "../../../../services/clients.service";
import {ClientDto} from "../../../../dtos/client-dto.model";
import {InstitutionService} from "../../../../services/institution.service";
import {GoalDto} from "../../../../dtos/goal-dto.model";
import {GoalService} from "../../../../services/goal.service";
import {AssistancePlanHourDto} from "../../../../dtos/assistance-plan-hour-dto.model";
import {AssistancePlanInfoForm} from "../assistance-plan-info-form";
import {DetailPageComponent} from "../../../detail-page.component";
import {HelperService} from "../../../../services/helper.service";
import {AssistancePlanDto} from "../../../../dtos/assistance-plan-dto.model";

@Component({
  selector: 'app-assistance-plan-detail',
  templateUrl: './assistance-plan-detail.component.html',
  styleUrls: ['./assistance-plan-detail.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS],
    },
    {provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS},
  ],
})
export class AssistancePlanDetailComponent extends DetailPageComponent<AssistancePlanView> implements OnInit {

  // CONST
  private validTabIndices = [0,1,2,3,4];
  private tabParamName = 'tab'

  // CONFIG
  tableColumns = ['title', 'description', 'hours', 'institution', 'action'];

  // VAR
  institutions: InstitutionDto[] = [];
  sponsors: SponsorDto[] = [];
  client: ClientDto = new ClientDto();
  dto$ = new ReplaySubject<AssistancePlanDto>();
  tabIndex = 0;

  // FORMs
  infoForm = new AssistancePlanInfoForm(true);

  constructor(
    private assistancePlanService: AssistancePlanService,
    private sponsorService: SponsorService,
    private clientService: ClientsService,
    private userService: UserService,
    private institutionService: InstitutionService,
    private goalService: GoalService,
    private route: ActivatedRoute,
    private converter: Converter,
    override helperService: HelperService
  ) {
    super(helperService);
  }

  override ngOnInit() {
    this.executeURLParams();
    super.ngOnInit();
  }

  executeURLParams() {
    this.route.params.subscribe(params => {
      // which tab should be open param
      if (params[this.tabParamName]) {
        const urlTabIndex = +params[this.tabParamName]
        if (!this.validTabIndices.includes(urlTabIndex, 0)) {
          return;
        }

        this.tabIndex = urlTabIndex;
      }
    });
  }

  loadValues() {
    // get id
    const id = this.route.snapshot.paramMap.get('id');

    if (id !== null) {
      combineLatest([
        this.sponsorService.allValues$,
        this.assistancePlanService.getById(+id),
        this.userService.affiliatedInstitutions$,
        this.userService.isAdmin$,
        this.institutionService.allValues$
      ])
        .subscribe(([sponsors, plan, affiliatedInstitutions,isAdmin, institutions]) => {
          this.value = <AssistancePlanView> {
            dto: plan,
            editable: isAdmin || affiliatedInstitutions.some(value => value === plan.institutionId)};
          this.dto$.next(plan);
          this.value$.next(this.value);
          this.editValue = <AssistancePlanView> {...this.value};
          this.institutions = institutions;
          this.sponsors = sponsors;

          this.loadClient(plan.clientId);
          this.refreshForm();
        })
    }
  }

  loadClient(id: number) {
    this.clientService.getById(id).subscribe(value => this.client = value);
  }

  getNewValue(): AssistancePlanView {
    return new AssistancePlanView();
  }

  refreshForm() {
    this.infoForm.start.setValue(this.value.dto.start);
    this.infoForm.end.setValue(this.value.dto.end);
    this.infoForm.sponsor.setValue(this.value.dto.sponsorId);
  }

  initFormSubscriptions() {
    this.infoForm.start.valueChanges
      .subscribe(value => this.value.dto.start = this.converter.formatDate(new Date(value)));
    this.infoForm.end.valueChanges
      .subscribe(value => this.value.dto.end = this.converter.formatDate(new Date(value)));
    this.infoForm.sponsor.valueChanges
      .subscribe(value => this.value.dto.sponsorId = value);
  }

  update() {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.assistancePlanService.update(this.value.dto.id, this.value.dto).subscribe({
      next: () => this.handleSuccess("Hilfeplan gespeichert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  createGoal(goal: GoalDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.goalService.create(goal).subscribe({
      next: () => this.handleSuccess("Ziel gespeichert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  updateGoal(goal: GoalDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.goalService.update(goal.id, goal).subscribe({
      next: () => this.handleSuccess("Ziel geändert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  deleteGoal(goal: GoalDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.goalService.delete(goal.id).subscribe({
      next: () => this.handleSuccess("Ziel gelöscht"),
      error: () => this.handleFailure("Fehler beim löschen")
    });
  }

  addAssistancePlanHour(value: AssistancePlanHourDto) {
    this.value.dto.hours.push(value);
    this.update();
  }

  updateAssistancePlanHour(value: AssistancePlanHourDto) {
    const index = this.value.dto.hours.findIndex(x => x.id == value.id);

    if (index >= 0) {
      this.value.dto.hours[index] = value;
      this.update();
    }
  }

  deleteAssistancePlanHour(value: AssistancePlanHourDto) {
    this.value.dto.hours = this.value.dto.hours.filter(x => x.id != value.id);
    this.update();
  }

  getInstitutionName(id: number): string {
    return this.institutions.find(value => value.id == id)?.name ?? "n/a";
  }

  getSponsorName(id: number): string {
    return this.sponsors.find(value => value.id == id)?.name ?? "n/a";
  }
}
