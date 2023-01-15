import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ContingentDto} from "../../../dtos/contingent-dto.model";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EmployeeDto} from "../../../dtos/employee-dto.model";
import {InstitutionDto} from "../../../dtos/institution-dto.model";
import {combineLatest, ReplaySubject} from "rxjs";
import {ContingentsService} from "../../../services/contingents.service";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {InstitutionService} from "../../../services/institution.service";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../comparer.helper";
import * as moment from 'moment';
import {EmployeeService} from "../../../services/employee.service";
import {TablePageComponent} from "../table-page.component";
import {HelperService} from "../../../services/helper.service";
import {Converter} from "../../converter.helper";
import {UserService} from "../../../services/user.service";
import {InstitutionView} from "../../../models/institution-view.model";
import {EmployeeView} from "../../../models/employee-view.model";
import {createStartEndValidator} from "../../validators/start-end-validator";
import {createMatchingPasswordsValidator} from "../../validators/matching-passwords.validator";

@Component({
  selector: 'app-contingents',
  templateUrl: './contingents.component.html',
  styleUrls: ['./contingents.component.css'],
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
export class ContingentsComponent
  extends TablePageComponent<ContingentDto, [EmployeeDto, InstitutionDto, ContingentDto, boolean]>
  implements OnInit {

  // INPUTS OUTPUTS
  @Input() employeeView$: ReplaySubject<EmployeeView> = new ReplaySubject<EmployeeView>();
  @Input() institutionView$: ReplaySubject<InstitutionView> = new ReplaySubject<InstitutionView>();
  @Input() hideEmployeeColumn: boolean = false;
  @Input() hideInstitutionColumn: boolean = false;
  @Input() hideAddButton: boolean = false;
  @Output() addedValueEvent = new EventEmitter<ContingentDto>();
  @Output() updatedValueEvent = new EventEmitter<ContingentDto>();
  @Output() deletedValueEvent = new EventEmitter<ContingentDto>();

  // VARs
  tableColumns = ['employee', 'institution', 'start', 'end', 'hours', 'actions'];
  employeeView: EmployeeView | null = null;
  institutionView: InstitutionView | null = null;
  leadingInstitutions : InstitutionDto[] = [];
  employees : EmployeeDto[] = [];
  institutions: InstitutionDto[] = [];
  contingents : [EmployeeDto, InstitutionDto, ContingentDto, boolean][] = [];

  // FILTER
  filterDate : Date | null = null;
  filterInstitutionId : number | null = null;
  filterEmployeeId : number | null = null;

  // FORMs
  infoForm = new FormGroup({
    start: new FormControl({value:Date.now(), disabled: false}, Validators.compose([
        Validators.required])),
    end: new FormControl({value:null, disabled: false}),
    hours: new FormControl({value:0, disabled: false}, Validators.compose([
      Validators.required])),
    institution: new FormControl()
  }, { validators: createStartEndValidator })

  override filterForm = new FormGroup({
    searchString: new FormControl(""),
    date: new FormControl(),
    institution: new FormControl(),
    employee: new FormControl(),
  });

  public get startControl() { return this.infoForm.controls['start']; }

  public get endControl() { return this.infoForm.controls['end']; }

  public get hoursControl() { return this.infoForm.controls['hours']; }

  public get institutionControl() { return this.infoForm.controls['institution']; }

  public get dateFilterControl() { return this.filterForm.controls['date']; }

  public get institutionFilterControl() { return this.filterForm.controls['institution']; }

  public get employeeFilterControl() { return this.filterForm.controls['employee']; }

  constructor(
    private contingentService: ContingentsService,
    private institutionService: InstitutionService,
    private employeeService: EmployeeService,
    private comparer: Comparer,
    private converter: Converter,
    private userService: UserService,
    override modalService: NgbModal,
    override helperService: HelperService) {
    super(modalService, helperService);
  }

  getNewValue(): ContingentDto {
    const dto = new ContingentDto();
    dto.employeeId = this.employeeView?.dto.id ?? 0;

    return dto;
  }

  loadValues() {
    // load institutions
    combineLatest([
      this.institutionService.allValues$,
      this.userService.leadingInstitutions$
    ])
      .subscribe(([institutions, leadingIds]) => {
        this.institutions = institutions;
        this.leadingInstitutions = institutions.filter(x => leadingIds.some(y => y == x.id));
      });

    // load employees
    this.employeeService
      .allValues$
      .subscribe(values => this.employees = values);

    // load contingents by employee
    this.employeeView$.subscribe({
      next: (value) => {
        this.institutionView = null;
        this.employeeView = value;
        this.hideAddButton = !this.employeeView?.editable ?? true;

        this.loadContingents();
      }
    })

    // load contingents by institution
    this.institutionView$.subscribe({
      next: (value) => {
        this.institutionView = value;
        this.employeeView = null;
        this.hideAddButton = !this.institutionView?.editable ?? true;

        this.loadContingents();
      }
    })
  }

  loadContingents() {
    if (this.employeeView != null) {
      this.contingentService
        .getCombinationByEmployeeId(this.employeeView.dto.id)
        .subscribe((contingents) => {
          this.sourceTableData = contingents;
          this.setTableData(contingents);
        });
    }
    else if (this.institutionView != null) {
      this.contingentService
        .getCombinationByInstitutionId(this.institutionView.dto.id)
        .subscribe({
          next: (values) => {
            this.sourceTableData = values;
            this.setTableData(values);
          }
        })
    }
  }

  initFormSubscriptions() {
    this.hoursControl.valueChanges.subscribe(value => this.editValue.weeklyServiceHours = value);
    this.startControl.valueChanges.subscribe(value => {
      if (value != null) {
        this.editValue.start = this.converter.formatDate(new Date(value.toString()))
      }
    });
    this.endControl.valueChanges.subscribe(value => {
      if (value != null) {
        this.editValue.end = this.converter.formatDate(new Date(value.toString()))
      } else {
        this.editValue.end = null;
      }
    });
    this.institutionControl.valueChanges.subscribe(value => {
      if (value != null) {
        this.editValue.institutionId = value;
      }
    });
  }

  override initFilterFormSubscriptions() {
    super.initFilterFormSubscriptions();

    this.dateFilterControl.valueChanges.subscribe((value) => this.setFilterDate(value));
    this.institutionFilterControl.valueChanges.subscribe((value) => this.setFilterInstitution(value));
    this.employeeFilterControl.valueChanges.subscribe((value) => this.setFilterInstitution(value));
  }

  fillEditForm(value: ContingentDto) {
    this.hoursControl.setValue(value.weeklyServiceHours);
    this.startControl.setValue(value.start);
    this.endControl.setValue(value.end);
    this.institutionControl.setValue(value.institutionId);
  }

  setTableData(combination) {
    this.filteredTableData = combination;
    this.tableSource.data = this.filteredTableData;

    this.refreshTablePage();
  }

  resetEnd() {
    this.endControl.setValue(null);
  }

  setFilterDate(event) {
    this.filterDate = event != null ? new Date(event.toString()) : null;
    this.filterTableData();
  }

  resetFilterDate() {
    this.dateFilterControl.setValue(null);
  }

  setFilterInstitution(value) {
    this.filterInstitutionId = value != null ? value : null;
    this.filterTableData();
  }

  resetFilterInstitution() {
    this.institutionFilterControl.setValue(null);
  }

  setFilterEmployee(value) {
    this.filterEmployeeId = value != null ? value : null;
    this.filterTableData();
  }

  resetFilterEmployee() {
    this.employeeFilterControl.setValue(null);
  }

  filterTableData() {
    let filterData = this.sourceTableData;

    // filter by date
    if (this.filterDate != null) {
      filterData = filterData.filter(x => {
        if (x[2].end != null)
          return moment(this.filterDate)
            .isBetween(new Date(x[2].start), new Date(x[2].end), "date", "[]");

        return moment(this.filterDate)
          .isAfter(new Date(x[2].start), "date");
      });
    }

    // filter by institution
    if (this.filterInstitutionId != null) {
      filterData = filterData.filter(x => x[1].id == this.filterInstitutionId);
    }

    // filter by employee
    if (this.filterEmployeeId != null) {
      filterData = filterData.filter(x =>x[0].id === this.filterEmployeeId);
    }

    this.filteredTableData = filterData;
    this.setTableData(this.filteredTableData);
  }

  create(contingent: ContingentDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.contingentService
      .create(contingent)
      .subscribe({
        next: (value) => {
          this.addedValueEvent.emit(value);
          this.handleSuccess("Kontingent gespeichert");
          },
        error: () => this.handleFailure("Fehler beim speichern")
      })
  }

  update(contingent: ContingentDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.contingentService
      .update(contingent.id, contingent)
      .subscribe({
        next: (value) => {
          this.updatedValueEvent.emit(value);
          this.handleSuccess("Kontingent gespeichert");
          },
        error: () => this.handleFailure("Fehler beim speichern")
      })
  }

  delete(contingent: ContingentDto) {
    this.contingentService
      .delete(contingent.id)
      .subscribe({
        next: (value) => {
          this.updatedValueEvent.emit(value);
          this.handleSuccess("Kontingent gelöscht");
        },
        error: () => this.handleFailure("Fehler beim löschen")
      })
  }

  fillTable(values : [EmployeeDto, InstitutionDto, ContingentDto, boolean][]) {
    this.contingents = values;
    this.tableSource.data = values
  }

  getLocalDateString(date: string | null) : string {
    return this.converter.getLocalDateString(date);
  }

  sortData(sort: Sort) {
    const data = this.contingents.slice();
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
          return this.comparer.compare(a[2].start, b[2].start, isAsc);
        case this.tableColumns[3]:
          return this.comparer.compare(a[2].end ?? "", b[2].end ?? "", isAsc);
        case this.tableColumns[4]:
          return this.comparer.compare(a[2].weeklyServiceHours, b[2].weeklyServiceHours, isAsc);
        default:
          return 0;
      }
    });
  }
}
