import { Component, OnInit } from '@angular/core';
import {AssistancePlanDto} from "../../../../dtos/assistance-plan-dto.model";
import {InstitutionDto} from "../../../../dtos/institution-dto.model";
import {SponsorDto} from "../../../../dtos/sponsor-dto.model";
import {InstitutionService} from "../../../../services/institution.service";
import {SponsorService} from "../../../../services/sponsor.service";
import {AssistancePlanService} from "../../../../services/assistance-plan.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Location} from "@angular/common";
import {ClientDto} from "../../../../dtos/client-dto.model";
import {ClientsService} from "../../../../services/clients.service";
import {ActivatedRoute} from "@angular/router";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from "@angular/material/core";
import {UserService} from "../../../../services/user.service";
import {combineLatest, ReplaySubject} from "rxjs";
import {Converter} from "../../../../services/converter.helper";
import {NewPageComponent} from "../../../new-page.component";
import {HelperService} from "../../../../services/helper.service";
import {AssistancePlanInfoForm} from "../assistance-plan-info-form";
import {AssistancePlanView} from "../../../../models/assistance-plan-view.model";
import {AssistancePlanHourDto} from "../../../../dtos/assistance-plan-hour-dto.model";

@Component({
    selector: 'app-assistance-plan-new',
    templateUrl: './assistance-plan-new.component.html',
    styleUrls: ['./assistance-plan-new.component.css'],
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
export class AssistancePlanNewComponent extends NewPageComponent<AssistancePlanDto> implements OnInit {
  // VARs
  valueView$: ReplaySubject<AssistancePlanView> = new ReplaySubject<AssistancePlanView>();
  client: ClientDto = new ClientDto();
  institutions: InstitutionDto[] = [];
  sponsors: SponsorDto[] = [];
  affiliatedInstitutions: InstitutionDto[] = [];

  // FORMs
  generalForm = new AssistancePlanInfoForm();

  constructor(
    private institutionService: InstitutionService,
    private sponsorService: SponsorService,
    private assistancePlanService: AssistancePlanService,
    private modal: NgbModal,
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

  getNewValue(): AssistancePlanDto {
    return new AssistancePlanDto();
  }

  loadReferenceValues() {
    combineLatest([
      this.institutionService.allValues$,
      this.sponsorService.allValues$,
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ])
      .subscribe(([institutions, sponsors, affiliatedIds, isAdmin]) => {
        this.valueView$.next(<AssistancePlanView> {
          dto: this.value,
          editable: true
        });
        this.institutions = institutions;
        this.sponsors = sponsors;
        this.affiliatedInstitutions = this.institutions.filter(value => isAdmin || affiliatedIds.some(id => id === value.id));
      });

    this.loadClient();
  }

  loadClient() {
    const id = this.route.snapshot.paramMap.get('id') ?? 0;

    this.clientService.getById(+id).subscribe({
      next: (value) => {
        if (value == null) {
          this.handleFailure("Fehler beim laden des Klienten", true);
        } else {
          this.client = value;
        }
      },
      error: () => this.handleFailure("Fehler beim laden des Klienten", true)
    });
  }

  initFormSubscriptions() {
    this.generalForm.start.valueChanges
      .subscribe(value => this.value.start = this.converter.formatDate(new Date(value)));
    this.generalForm.end.valueChanges
      .subscribe(value => this.value.end = this.converter.formatDate(new Date(value)));
    this.generalForm.sponsor.valueChanges
      .subscribe(value => this.value.sponsorId = this.sponsors.find(sponsor => sponsor.id === value)?.id ?? 0);
    this.generalForm.institution.valueChanges
      .subscribe(value => this.value.institutionId = this.institutions.find(inst => inst.id === value)?.id ?? 0);
  }

  create() {
    this.value.clientId = this.client.id;

    this.assistancePlanService
      .create(this.value)
      .subscribe({
        next: () => this.handleSuccess("Hilfeplan gespeichert", true),
        error: () => this.handleSuccess("Fehler beim speichern")
    });
  }


  addAssistancePlanHour(value: AssistancePlanHourDto) {
    this.value.hours.push(value);
  }

  updateAssistancePlanHour(value: AssistancePlanHourDto) {
    const index = this.value.hours.findIndex(x => x.id == value.id);

    if (index >= 0) {
      this.value.hours[index] = value;
    }
  }

  deleteAssistancePlanHour(value: AssistancePlanHourDto) {
    this.value.hours = this.value.hours.filter(x => x.id != value.id);
  }
}
