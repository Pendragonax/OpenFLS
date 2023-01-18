import { Component, OnInit } from '@angular/core';
import {NewPageComponent} from "../../shared/modules/new-page.component";
import {ServiceDto} from "../../dtos/service-dto.model";
import {InstitutionService} from "../../services/institution.service";
import {ClientsService} from "../../services/clients.service";
import {AssistancePlanService} from "../../services/assistance-plan.service";
import {HourTypeService} from "../../services/hour-type.service";
import {CategoriesService} from "../../services/categories.service";
import {UserService} from "../../services/user.service";
import {InstitutionDto} from "../../dtos/institution-dto.model";
import {combineLatest, startWith} from "rxjs";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ClientDto} from "../../dtos/client-dto.model";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {AssistancePlanDto} from "../../dtos/assistance-plan-dto.model";
import {Converter} from "../../shared/converter.helper";
import {GoalDto} from "../../dtos/goal-dto.model";
import {HourTypeDto} from "../../dtos/hour-type-dto.model";
import {CategoryDto} from "../../dtos/category-dto.model";
import {SponsorDto} from "../../dtos/sponsor-dto.model";
import {SponsorService} from "../../services/sponsor.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ServiceService} from "../../services/service.service";
import {GoalHourDto} from "../../dtos/goal-hour-dto.model";
import {AssistancePlanHourDto} from "../../dtos/assistance-plan-hour-dto.model";
import {Location} from "@angular/common";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-service-detail',
  templateUrl: './service-detail.component.html',
  styleUrls: ['./service-detail.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS],
    },
    {provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS},
  ]
})
export class ServiceDetailComponent extends NewPageComponent<ServiceDto> implements OnInit {

  // STATESs
  editMode = false;

  // VARs
  institutions: InstitutionDto[] = []
  hourTypes: HourTypeDto[] = []
  clients: ClientDto[] = [];
  sponsors: SponsorDto[] = [];
  categories: CategoryDto[] = [];
  filteredClients: ClientDto[] = [];
  filteredAssistancePlans: AssistancePlanDto[] = [];
  selectedStartDate: string = this.converter.formatDate(new Date(Date.now()));
  selectedEndDate: string = this.converter.formatDate(new Date(Date.now()));
  selectedClient: ClientDto = new ClientDto();
  selectedAssistancePlan: AssistancePlanDto | null | undefined = null;
  selectedGoals: GoalDto[] = [];
  selectedCategories: CategoryDto[] = [];
  clientSelected: boolean = false;
  assistancePlanSelected: boolean = false;
  timeNow = new Date(Date.now());
  minDate = new Date();

  // FORMS
  firstForm = new FormGroup({
    startDate: new FormControl({value: this.timeNow, disabled: true},
      Validators.compose([Validators.required])),
    startHour: new FormControl(this.timeNow.getHours(),
      Validators.compose([Validators.required, Validators.max(23), Validators.min(0)])),
    startMinute: new FormControl(this.timeNow.getMinutes(),
      Validators.compose([Validators.required, Validators.max(59), Validators.min(0)])),
    endDate: new FormControl({value: this.timeNow, disabled: true},
      Validators.compose([Validators.required])),
    endHour: new FormControl(this.timeNow.getHours(),
      Validators.compose([Validators.required, Validators.max(23), Validators.min(0)])),
    endMinute: new FormControl(this.timeNow.getMinutes(),
      Validators.compose([Validators.required, Validators.max(59), Validators.min(0)])),
    institution: new FormControl(null,
      Validators.compose([Validators.required])),
    hourType: new FormControl(null,
      Validators.compose([Validators.required]))
  });

  secondForm = new FormGroup({
    client: new FormControl({value: '', disabled: true }),
    clientList: new FormControl({value: '', disabled: true }),
    assistancePlanList: new FormControl({value: '', disabled: true }),
    goalList: new FormControl(null)
  });

  thirdForm = new FormGroup({
    categoryList: new FormControl(null),
    title: new FormControl("",
      Validators.compose([Validators.max(64)])),
    content: new FormControl("",
      Validators.compose([Validators.max(1024)])),
    unfinished: new FormControl(false)
  });

  // GETTER
  get startDateControl() { return this.firstForm.controls['startDate']; }

  get startHourControl() { return this.firstForm.controls['startHour']; }

  get startMinuteControl() { return this.firstForm.controls['startMinute']; }

  get endDateControl() { return this.firstForm.controls['endDate']; }

  get endHourControl() { return this.firstForm.controls['endHour']; }

  get endMinuteControl() { return this.firstForm.controls['endMinute']; }

  get institutionControl() { return this.firstForm.controls['institution']; }

  get hourTypeControl() { return this.firstForm.controls['hourType']; }

  get assistancePlansControl() { return this.secondForm.controls['assistancePlanList']; }

  get goalsControl() { return this.secondForm.controls['goalList']; }

  get clientControl() { return this.secondForm.controls['client']; }

  get clientsControl() { return this.secondForm.controls['clientList']; }

  get categoriesControl() { return this.thirdForm.controls['categoryList']; }

  get titleControl() { return this.thirdForm.controls['title']; }

  get contentControl() { return this.thirdForm.controls['content']; }

  get unfinishedControl() { return this.thirdForm.controls['unfinished']; }

  constructor(
    private userService: UserService,
    private institutionService: InstitutionService,
    private clientService: ClientsService,
    private assistancePlanService: AssistancePlanService,
    private hourTypeService: HourTypeService,
    private categoryService: CategoriesService,
    private sponsorService: SponsorService,
    private serviceService: ServiceService,
    private router: Router,
    private route: ActivatedRoute,
    private converter: Converter,
    override helperService: HelperService,
    override location: Location
  ) {
    super(helperService, location);
  }

  override ngOnInit() {
    this.loadValues();
  }

  getNewValue(): ServiceDto {
    return new ServiceDto();
  }

  loadValues() {
    // get id
    const id = this.route.snapshot.paramMap.get('id');

    if (id != null) {
      this.editMode = true;
      this.serviceService.getById(+id)
        .subscribe({
          next: (value) => this.loadEditBaseValues(value),
          error: () => this.handleFailure("Fehler beim laden der Dokumentation", true)
        });
    } else {
      this.loadReferenceValues();
    }
  }

  loadReferenceValues() {
    combineLatest([
      this.userService.writeableInstitutions$,
      this.institutionService.allValues$,
      this.clientService.allValues$,
      this.hourTypeService.allValues$,
      this.userService.user$,
      this.sponsorService.allValues$
    ])
      .subscribe(([writeableInstitutions, institutions, clients, hourTypes, user, sponsors]) => {
        // base
        this.institutions = institutions.filter(value => writeableInstitutions.some(inst => inst == value.id));
        this.clients = clients;
        this.filteredClients = clients.slice(0, 5);
        this.hourTypes = hourTypes;
        this.sponsors = sponsors;

        // service
        this.value.employeeId = user.id;

        this.initFormSubscriptions();
      });
  }

  loadEditBaseValues(service: ServiceDto) {
    combineLatest([
      this.userService.writeableInstitutions$,
      this.institutionService.allValues$,
      this.clientService.allValues$,
      this.hourTypeService.allValues$,
      this.sponsorService.allValues$,
      this.clientService.getById(service.clientId),
    ])
      .subscribe(([writeableInstitutions, institutions, clients, hourTypes, sponsors, client]) => {
        // base
        this.institutions = institutions.filter(value => writeableInstitutions.some(inst => inst == value.id));
        this.clients = clients;
        this.filteredClients = clients.filter(value => value.id == service.clientId);
        this.hourTypes = hourTypes;
        this.sponsors = sponsors;

        // service
        this.value = service;
        this.selectedClient = client;
        this.clientSelected = true;
        this.selectedStartDate = this.converter.formatDate(new Date(service.start.toString()))
        this.selectedEndDate = this.converter.formatDate(new Date(service.end.toString()))

        this.selectedAssistancePlan = client.assistancePlans.find(value => value.id == service.assistancePlanId);
        this.filteredAssistancePlans = [this.selectedAssistancePlan ?? new AssistancePlanDto()];
        this.assistancePlanSelected = true;
        this.setGoals(service.goals.map(value => value.id));

        this.categories = client.categoryTemplate.categories;
        this.selectedCategories = this.categories.filter(value => service.categorys.some(category => category.id == value.id));

        this.fillFormGroups();
        this.initFormSubscriptions();
      });
  }

  loadClient(id: number) {
    this.clientService.getById(id).subscribe(value => {
      this.selectedClient = value;
      this.filteredAssistancePlans = this.getAssistancePlansByDateString(value.assistancePlans, this.startDateControl.value);
      this.categories = value.categoryTemplate.categories;
    })
  }

  fillFormGroups() {
    this.startDateControl.setValue(this.converter.formatDate(new Date(this.value.start)));
    this.startHourControl.setValue(new Date(this.value.start).getHours());
    this.startMinuteControl.setValue(new Date(this.value.start).getMinutes());

    this.endDateControl.setValue(this.converter.formatDate(new Date(this.value.end)));
    this.endHourControl.setValue(new Date(this.value.end).getHours());
    this.endMinuteControl.setValue(new Date(this.value.end).getMinutes());

    this.institutionControl.setValue(this.value.institutionId);
    this.hourTypeControl.setValue(this.value.hourTypeId);

    this.clientsControl.setValue([this.value.clientId]);
    this.assistancePlansControl.setValue([this.value.assistancePlanId]);
    this.goalsControl.setValue(this.selectedGoals.map(value => value.id));
    this.categoriesControl.setValue(this.selectedCategories.map(value => value.id));

    this.titleControl.setValue(this.value.title);
    this.contentControl.setValue(this.value.content);
    this.unfinishedControl.setValue(this.value.unfinished);
  }

  override initFormSubscriptions() {
    // only on EditMode
    if (!this.editMode) {
      this.startDateControl.enable();
      this.startDateControl.valueChanges
        .subscribe((value) => {
          if (value != null) {
            this.selectedStartDate = this.converter.formatDate(new Date(value.toString()));
            this.minDate = new Date(value.toString());
          }

          this.endDateControl.setValue(value);
        });

      this.startHourControl.valueChanges.subscribe(value => this.endHourControl.setValue(value));
      this.startMinuteControl.valueChanges.subscribe(value => this.endMinuteControl.setValue(value));

      this.endDateControl.enable();
      this.endDateControl.valueChanges
        .subscribe((value) => {
          if (value != null) {
            this.selectedEndDate = this.converter.formatDate(new Date(value.toString()));
          }
        })

      this.clientControl.enable();
      this.clientControl.valueChanges.pipe(startWith('')).subscribe(value => {
        this.filteredClients = this._getClients(value || '');
        this.clientsControl.setValue(0);
        this.value.clientId = 0;

        this.filteredAssistancePlans = [];
        this.selectAssistancePlan(null);
      });

      this.clientsControl.enable();
      this.clientsControl.valueChanges
        .subscribe((value: number[]) => {
          this.resetAssistancePlan();
          this.assistancePlansControl.setValue("")
          this.resetGoals();
          this.goalsControl.setValue("");
          this.resetCategories();
          this.categoriesControl.setValue("");

          if (value.length > 0) {
            this.value.clientId = value[0];
            this.clientSelected = true;
            this.loadClient(value[0]);
          } else {
            this.value.clientId = 0;
            this.clientSelected = false;
          }
        });

      this.assistancePlansControl.enable();
      this.assistancePlansControl.valueChanges
        .subscribe((value: number[]) => {
          this.resetGoals();
          this.goalsControl.setValue("");

          if (value.length > 0) {
            this.selectAssistancePlan(this.selectedClient.assistancePlans.find(plan => plan.id == value[0]));
          } else {
            this.selectAssistancePlan(null);
          }
        });
    }

    // general
    this.hourTypeControl.valueChanges.subscribe(value => this.value.hourTypeId = value);
    this.institutionControl.valueChanges.subscribe(value => this.value.institutionId = value);

    this.goalsControl.valueChanges
      .subscribe((value: number[]) => {
        if (value.length > 0) {
          this.setGoals(value);
        } else {
          this.resetGoals();
        }
      });

    this.categoriesControl.valueChanges
      .subscribe((value: number[]) => {
        if (value.length > 0) {
          this.setCategories(value);
        } else {
          this.resetCategories();
        }
      });

    this.contentControl.valueChanges.subscribe(value => this.value.content = value);
    this.titleControl.valueChanges.subscribe(value => this.value.title = value);
    this.unfinishedControl.valueChanges.subscribe(value => this.value.unfinished = value);
  }

  selectAssistancePlan(plan: AssistancePlanDto | undefined | null) {
    if (plan != null) {
      this.assistancePlanSelected = true;
      this.value.assistancePlanId = plan.id;
      this.selectedAssistancePlan = plan;
    } else {
      this.assistancePlanSelected = false;
      this.value.assistancePlanId = 0;
      this.selectedAssistancePlan = null;
    }
  }

  resetAssistancePlan() {
    this.assistancePlanSelected = false;
    this.value.assistancePlanId = 0;
    this.selectedAssistancePlan = null;
  }

  setGoals(ids: number[]) {
    this.selectedGoals = this.selectedAssistancePlan?.goals.filter(value => ids.some(id => value.id == id)) ?? [];
    this.value.goals = this.selectedGoals;
  }

  resetGoals() {
    this.selectedGoals = [];
    this.value.goals = [];
  }

  setCategories(ids: number[]) {
    this.selectedCategories = this.categories.filter(value => ids.some(id => id == value.id));
    this.value.categorys = this.selectedCategories;
  }

  resetCategories() {
    this.selectedCategories = [];
    this.value.categorys = [];
  }

  create() {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.value.start = this.converter.getDateTimeString(
      this.selectedStartDate,
      this.startHourControl.value,
      this.startMinuteControl.value);

    this.value.end = this.converter.getDateTimeString(
      this.selectedEndDate,
      this.endHourControl.value,
      this.endMinuteControl.value);

    if (this.editMode) {
      this.serviceService.update(this.value.id, this.value).subscribe({
        next: () => this.handleSuccess("Dokumentation gespeichert", true),
        error: () => this.handleFailure("Fehler beim speichern")
      });
    } else {
      this.serviceService.create(this.value).subscribe({
        next: () => this.handleSuccess("Dokumentation gespeichert", false, true),
        error: () => this.handleFailure("Fehler beim speichern")
      });
    }
  }

  formatDateStringToGermanDateString(dateString: string): string {
    return this.converter.formatDateToGerman(new Date(dateString));
  }

  getAssistancePlansByDateString(assistancePlans: AssistancePlanDto[], searchDateString: string) : AssistancePlanDto[] {
    return assistancePlans.filter(value => {
      const startDate = this.converter.formatDate(new Date(value.start));
      const endDate = this.converter.formatDate(new Date(value.end));
      const date = this.converter.formatDate(new Date(searchDateString))
      return startDate <= date && endDate >= date
    });
  }

  getDateString(plan: AssistancePlanDto | undefined): string {
    if (plan != null) {
      return `${this.formatDateStringToGermanDateString(plan.start)} - ${this.formatDateStringToGermanDateString(plan.end)}`;
    }

    return "n/a";
  }

  getSponsorName(plan: AssistancePlanDto | undefined): string {
    if (plan != null) {
      return `${this.sponsors.find(value => value.id == plan.sponsorId)?.name ?? "n/a"}`;
    }

    return "n/a";
  }

  sumGoalHours(hours: GoalHourDto[]) {
    if (hours.length == 0)
      return "n/a";

    return hours
      .map(value => value.weeklyHours)
      .reduce((sum, current) => sum + current)
      .toFixed(2);
  }

  sumAssistancePlanHours(hours: AssistancePlanHourDto[]) {
    if (hours.length == 0)
      return "n/a";

    return hours
      .map(value => value.weeklyHours)
      .reduce((sum, current) => sum + current)
      .toFixed(2);
  }

  private _getClients(searchString: string): ClientDto[] {
    let filterValue = searchString.toLowerCase();

    return this.clients
      .filter(client => client.firstName.toLowerCase().includes(filterValue) ||
        client.lastName.toLowerCase().includes(filterValue))
      .slice(0, 5);
  }
}
