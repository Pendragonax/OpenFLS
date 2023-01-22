import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {combineLatest, ReplaySubject} from "rxjs";
import {InstitutionDto} from "../../../dtos/institution-dto.model";
import {ClientDto} from "../../../dtos/client-dto.model";
import {AssistancePlanDto} from "../../../dtos/assistance-plan-dto.model";
import {FormControl, FormGroup} from "@angular/forms";
import {AssistancePlanService} from "../../../services/assistance-plan.service";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../comparer.helper";
import {InstitutionService} from "../../../services/institution.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import * as moment from "moment";
import {AssistancePlanView} from "../../../models/assistance-plan-view.model";
import {ClientView} from "../../../models/client-view.model";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {SponsorDto} from "../../../dtos/sponsor-dto.model";
import {InstitutionView} from "../../../models/institution-view.model";
import {TablePageComponent} from "../table-page.component";
import {HelperService} from "../../../services/helper.service";
import {Converter} from "../../converter.helper";
import {GoalDto} from "../../../dtos/goal-dto.model";
import {HourTypeDto} from "../../../dtos/hour-type-dto.model";
import {HourTypeService} from "../../../services/hour-type.service";

@Component({
  selector: 'app-assistance-plans',
  templateUrl: './assistance-plans.component.html',
  styleUrls: ['./assistance-plans.component.css'],
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
export class AssistancePlansComponent
  extends TablePageComponent<AssistancePlanDto, [ClientDto, InstitutionDto, SponsorDto, AssistancePlanView]>
  implements OnInit {

  // INPUTS OUTPUTS
  @Input() client$: ReplaySubject<ClientView> = new ReplaySubject<ClientView>();
  @Input() sponsor$: ReplaySubject<SponsorDto> = new ReplaySubject<SponsorDto>();
  @Input() institution$: ReplaySubject<InstitutionView> = new ReplaySubject<InstitutionView>();
  @Input() hideInstitutionColumn: boolean = false;
  @Input() hideClientColumn: boolean = false;
  @Input() hideSponsorColumn: boolean = false;
  @Input() hideAddButton: boolean = false;
  @Input() hideInstitutionFilter: boolean = false;
  @Input() hideSearchStringFilter: boolean = false;
  @Output() addedValueEvent = new EventEmitter<AssistancePlanDto>();
  @Output() updatedValueEvent = new EventEmitter<AssistancePlanDto>();
  @Output() deletedValueEvent = new EventEmitter<AssistancePlanDto>();

  // Config
  tableColumns = ['client', 'institution', 'sponsor', 'start', 'end', 'hours', 'actions'];

  // VARs
  client: ClientView = new ClientView();
  sponsor: SponsorDto = new SponsorDto();
  institution: InstitutionView = new InstitutionView();
  tableData: [ClientDto, InstitutionDto, SponsorDto, AssistancePlanView][] = [];
  institutions: InstitutionDto[] = [];
  hourTypes: HourTypeDto[] = [];
  modalGoal: GoalDto = new GoalDto();
  addAvailable: boolean = false;

  // FILTER
  filterDate : Date | null = null;
  filterInstitutionId : number | null = null;
  filterClientId : number | null = null;

  // FORMs
  override filterForm = new FormGroup({
    searchString: new FormControl(""),
    date: new FormControl(),
    institution: new FormControl(),
    client: new FormControl(),
  });

  get dateControl() { return this.filterForm.controls['date']; }

  get institutionControl() { return this.filterForm.controls['institution']; }

  get clientControl() { return this.filterForm.controls['client']; }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private assistancePlanService: AssistancePlanService,
    private institutionService: InstitutionService,
    private hourTypeService: HourTypeService,
    private comparer: Comparer,
    private converter: Converter
  ) {
    super(modalService, helperService);
  }

  getNewValue(): AssistancePlanDto {
    return new AssistancePlanDto();
  }

  loadValues() {
    this.client$.subscribe({
      next: (value) => {
        this.client = value;
        this.addAvailable = value.editable;
        this.loadValuesByClient();
      }
    });

    this.sponsor$.subscribe({
      next: (value) => {
        this.sponsor = value;
        this.addAvailable = false;
        this.loadValuesBySponsor();
      }
    });

    this.institution$.subscribe({
      next: (value) => {
        this.institution = value;
        this.addAvailable = false;
        this.loadValuesByInstitution();
      }
    });

    combineLatest([
      this.institutionService.allValues$,
      this.hourTypeService.allValues$
    ])
      .subscribe(([institutions, hourTypes]) => {
        this.institutions = institutions;
        this.hourTypes = hourTypes;
      });
  }

  loadValuesByClient() {
    this.assistancePlanService.getCombinationByClientId(this.client.dto.id).subscribe({
      next: (values) => this.setTableSource(values)
    });
  }

  loadValuesBySponsor() {
    this.assistancePlanService.getCombinationBySponsorId(this.sponsor.id).subscribe({
      next: (values) => this.setTableSource(values)
    });
  }

  loadValuesByInstitution() {
    this.assistancePlanService.getCombinationByInstitutionId(this.institution.dto.id).subscribe({
      next: (values) => this.setTableSource(values)
    });
  }

  initFormSubscriptions() {
  }

  fillEditForm(value: AssistancePlanDto) {
    throw new Error('Method not implemented.');
  }

  override initFilterFormSubscriptions() {
    super.initFilterFormSubscriptions();

    this.dateControl.valueChanges.subscribe((value) => this.setFilterDate(value));
    this.institutionControl.valueChanges.subscribe((value) => this.setFilterInstitution(value));
    this.clientControl.valueChanges.subscribe((value) => this.setFilterInstitution(value));
  }

  setModalGoal(goal: GoalDto) {
    this.modalGoal = goal;
  }

  setFilterDate(value) {
    if (value != null) {
      this.filterDate = new Date(value.toString());
      this.filterTableData();
      return;
    }

    this.filterDate = null;
  }

  resetFilterDate() {
    this.dateControl.setValue(null);
    this.filterDate = null;
    this.filterTableData();
  }

  setTableSource(assistancePlanCombination) {
    this.filteredTableData = assistancePlanCombination;
    this.tableData = this.filteredTableData;
    this.tableSource.data = this.filteredTableData;

    this.refreshTablePage();
  }

  setFilterInstitution(value) {
    if (value != null) {
      this.filterInstitutionId = value;
      this.filterTableData()
      return;
    }

    this.filterInstitutionId = null;
  }

  resetFilterInstitution() {
    this.institutionControl.setValue(null);
    this.filterInstitutionId = null;
    this.filterTableData()
  }

  create(value: AssistancePlanDto) {
    throw new Error('Method not implemented.');
  }

  update(value: AssistancePlanDto) {
    throw new Error('Method not implemented.');
  }

  delete(value: AssistancePlanDto) {
    this.assistancePlanService
      .delete(value.id)
      .subscribe({
        next: () => this.handleSuccess("Hilfeplan gelöscht"),
        error: () => this.handleFailure("Fehler beim löschen")
      })
  }

  filterTableData() {
    let filteredData = this.tableData.slice();

    // filter by searchString
    filteredData = filteredData.filter(x => {
      return x[0].firstName.toLowerCase().includes(this.searchString) ||
      x[0].lastName.toLowerCase().includes(this.searchString) ||
      x[1].name.toLowerCase().includes(this.searchString) ||
      x[2].name.toLowerCase().includes(this.searchString)
    });

    // filter by date
    if (this.filterDate != null) {
      filteredData = filteredData.filter(x => {
        if (this.filterDate == null)
          return true

        if (x[3].dto.end != null)
          return moment(this.filterDate)
            .isBetween(new Date(x[3].dto.start), new Date(x[3].dto.end), "date", "[]")

        return moment(this.filterDate)
          .isAfter(new Date(x[3].dto.start), "date")
      })
    }

    // filter by institution
    if (this.filterInstitutionId != null) {
      filteredData = filteredData
        .filter(x => this.filterInstitutionId == null || x[1].id == this.filterInstitutionId)
    }

    // filter by client
    if (this.filterClientId != null) {
      filteredData = filteredData
        .filter(x => this.filterClientId == null || x[2].id == this.filterClientId)
    }

    this.filteredTableData = filteredData;

    this.refreshTablePage();
  }

  override handleInformationModalClosed() {
    this.modalGoal = new GoalDto();
  }

  sortData(sort: Sort) {
    const data = this.tableData.slice();
    if (!sort.active || sort.direction === '') {
      this.tableSource.data = data;
      return;
    }

    this.tableSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case this.tableColumns[0]:
          return this.comparer.compare(a[0].lastName, b[0].lastName, isAsc);
        case this.tableColumns[1]:
          return this.comparer.compare(a[1].name, b[1].name, isAsc);
        case this.tableColumns[2]:
          return this.comparer.compare(a[2].name, b[2].name, isAsc);
        case this.tableColumns[3]:
          return this.comparer.compare(a[3].dto.start, b[3].dto.start, isAsc);
        case this.tableColumns[4]:
          return this.comparer.compare(a[3].dto.end ?? "", b[3].dto.end ?? "", isAsc);
        default:
          return 0;
      }
    });
  }

  getDateString(date: string | null) : string {
    return this.converter.getLocalDateString(date);
  }

  getHourTypeName(id: number): string {
    return this.hourTypes.find(x => x.id == id)?.title ?? "";
  }

  sumGoalsWeeklyHoursByPlan(plan: AssistancePlanDto): number {
    if (plan.goals == null || plan.goals.length <= 0)
      return 0;

    const goalHours = plan.goals
      .filter(goal => goal.hours != null && goal.hours.length > 0)
      .map(goal => goal.hours.map(x => x.weeklyHours).reduce((sum, current) => sum + current));

    return (goalHours.length > 0) ? goalHours.reduce((sum, current) => sum + current) : 0;
  }

  sumWeeklyHoursByPlan(plan: AssistancePlanDto): number {
    if (plan.hours == null || plan.hours.length <= 0)
      return 0;

    return plan.hours
      .map(value => value.weeklyHours)
      .reduce((sum, current) => sum + current);
  }
}
