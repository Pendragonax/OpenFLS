import {Component, Input, OnInit} from '@angular/core';
import {TablePageComponent} from "../table-page.component";
import {ServiceDto} from "../../../dtos/service-dto.model";
import {Sort} from "@angular/material/sort";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ServiceService} from "../../../services/service.service";
import {UserService} from "../../../services/user.service";
import {ReplaySubject, Subject, window} from "rxjs";
import {Converter} from "../../converter.helper";
import {ClientDto} from "../../../dtos/client-dto.model";
import {ClientsService} from "../../../services/clients.service";
import {combineLatest} from "rxjs";
import {HelperService} from "../../../services/helper.service";
import {InstitutionDto} from "../../../dtos/institution-dto.model";
import {InstitutionService} from "../../../services/institution.service";
import {EmployeeDto} from "../../../dtos/employee-dto.model";
import {EmployeeService} from "../../../services/employee.service";
import {FormControl, FormGroup} from "@angular/forms";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {Comparer} from "../../comparer.helper";

@Component({
  selector: 'app-service',
  templateUrl: './service.component.html',
  styleUrls: ['./service.component.css'],
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
export class ServiceComponent
  extends TablePageComponent<ServiceDto, [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean]>
  implements OnInit {

  @Input() clientId$: ReplaySubject<number> = new ReplaySubject<number>();
  @Input() employeeId$: ReplaySubject<number> = new ReplaySubject<number>();

  // CONFIG
  tableColumns: string[] = ['date', 'start', 'end', 'client', 'title', 'content', 'actions'];

  // VARs
  clientId: number | null = null;
  employeeId: number | null = null;
  editTableValue: [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean] =
    [new ClientDto(), new EmployeeDto(), new InstitutionDto(), new ServiceDto(), false];
  institutions$: ReplaySubject<InstitutionDto[]> = new ReplaySubject<InstitutionDto[]>();
  institutions: InstitutionDto[] = [];
  clients$: ReplaySubject<ClientDto[]> = new ReplaySubject<ClientDto[]>();
  clients: ClientDto[] = [];
  employees$: ReplaySubject<EmployeeDto[]> = new ReplaySubject();
  employees: EmployeeDto[] = [];
  user: EmployeeDto = new EmployeeDto();
  tableData: [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean][] = [];

  // FILTER-VARs
  filterDateStart: Date = new Date(Date.now());
  filterDateEnd: Date = new Date(Date.now());
  filteredValues: ServiceDto[] = [];
  filteredValues$: Subject<ServiceDto[]> = new Subject<ServiceDto[]>();

  // Form Groups
  dateFilterGroup = new FormGroup({
    start: new FormControl(new Date(Date.now())),
    end: new FormControl(new Date(Date.now()))
  });

  get dateFilterStartControl() { return this.dateFilterGroup.controls['start']; }
  get dateFilterEndControl() { return this.dateFilterGroup.controls['end']; }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private converter: Converter,
    private serviceService: ServiceService,
    private clientService: ClientsService,
    private institutionService: InstitutionService,
    private employeeService: EmployeeService,
    private comparer: Comparer,
    private userService: UserService
  ) {
    super(modalService, helperService);
  }

  override ngOnInit() {
    this.initFilterFormSubscriptions();
    this.initFormSubscriptions();
    this.loadReferenceValues();
  }

  override loadReferenceValues() {
    this.userService.user$.subscribe(value => {
      this.user = value;
    });

    this.clientService.allValues$.subscribe(values => {
      this.clients = values;
      this.clients$.next(values);
    });

    this.institutionService.allValues$.subscribe(values => {
      this.institutions = values;
      this.institutions$.next(values);
    });

    this.employeeService.allValues$.subscribe(values => {
      this.employees = values;
      this.employees$.next(values);
    })

    this.employeeId$.subscribe(value => {
      this.clientId = null;
      this.employeeId = value;
      this.loadServicesByEmployee();
    });

    this.clientId$.subscribe(value => {
      this.employeeId = null;
      this.clientId = value;
      this.loadServicesByClient();
    });
  }

  loadValues() {
    this.isSubmitting = true;
    this.searchString = "";

    if (this.clientId != null) {
      this.loadServicesByClient();
    }

    if (this.employeeId != null) {
      this.loadServicesByEmployee();
    }
  }

  loadServicesByEmployee() {
    combineLatest([
      this.serviceService.getByEmployeeAndStartAndEnd(this.employeeId ?? 0, this.filterDateStart, this.filterDateEnd),
      this.employeeId$
    ])
      .subscribe({
        next: ([services, employeeId]) => {
          this.employeeId = employeeId;
          this.values$.next(services);
          this.values = services;
          this.searchStringControl.setValue("");
          this.filteredValues = services;
          this.filteredValues$.next(services);
          this.isSubmitting = false;

          this.setTableSource(this.getTableData())
        },
        error: () => this.handleFailure("Fehler beim laden")
      });
  }

  loadServicesByClient() {
    combineLatest([
      this.serviceService.getByClientAndStartAndEnd(this.clientId ?? 0, this.filterDateStart, this.filterDateEnd),
      this.clientId$
    ])
      .subscribe({
        next: ([services, client]) => {
          this.clientId = client;
          this.values$.next(services);
          this.values = services;
          this.filteredValues = services;
          this.filteredValues$.next(services);
          this.searchStringControl.setValue("");
          this.isSubmitting = false;

          this.setTableSource(this.getTableData())
        },
        error: () => this.handleFailure("Fehler beim laden")
      });
  }

  filterTableData() {
    let filteredData = this.getTableData();

    // filter by searchString
    filteredData = filteredData.filter(x => {
      return x[3].title.toLowerCase().includes(this.searchString) ||
      x[3].content.toLowerCase().includes(this.searchString)
    });

    this.filteredTableData = filteredData;

    this.refreshTablePage();
  }

  getTableData(): [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean][] {
    return this.values
      .map(value => [
        this.clients.find(client => client.id == value.clientId) ?? new ClientDto(),
        this.employees.find(employee => employee.id == value.employeeId) ?? new EmployeeDto(),
        this.institutions.find(institution => institution.id == value.institutionId) ?? new InstitutionDto(),
        value,
        (this.user.access?.role ?? 99) == 1 || (value.employeeId == this.user.id && this.isEditableByDate(new Date(value.start)))
      ]);
  }

  create(value: ServiceDto) {
    throw new Error('Method not implemented.');
  }

  update(value: ServiceDto) {
    throw new Error('Method not implemented.');
  }

  delete(value: ServiceDto) {
    if (this.isSubmitting)
      return;

    this.serviceService.delete(value.id).subscribe({
      next: () => this.handleSuccess("Eintrag gelöscht"),
      error: () => this.handleFailure("Fehler beim löschen")
    });
  }

  fillEditForm(value: ServiceDto) {
    throw new Error('Method not implemented.');
  }

  getNewValue(): ServiceDto {
    return new ServiceDto();
  }

  initFormSubscriptions() {
    // no form is in use
    this.dateFilterStartControl.valueChanges.subscribe((value) => {
      this.filterDateStart = new Date(value);
      this.filterDateEnd = new Date(value);
      this.handleFilterDateChanged();
    });
    this.dateFilterEndControl.valueChanges.subscribe((value) => {
      this.filterDateEnd = new Date(value);
      this.handleFilterDateChanged();
    });
  }

  handleFilterDateChanged() {
    this.loadValues();
  }

  increaseFilterDate(days: number) {
    this.filterDateStart.setDate(this.filterDateStart.getDate() + days);
    this.filterDateEnd = this.filterDateStart;
    this.dateFilterStartControl.setValue(this.filterDateStart);
    this.dateFilterEndControl.setValue(this.filterDateEnd);
  }

  setFilterDateToToday() {
    this.filterDateStart = new Date(Date.now());
    this.filterDateEnd = this.filterDateStart;
    this.dateFilterStartControl.setValue(this.filterDateStart);
    this.dateFilterEndControl.setValue(this.filterDateEnd);
  }

  setTableSource(data) {
    this.filteredTableData = data;
    this.tableData = this.filteredTableData;
    this.tableSource.data = this.filteredTableData;

    this.refreshTablePage();
  }

  scrollUp() {
    document.body.scrollTop = 0; // For Safari
    document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
  }

  openServiceInformationModal(content, value: [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean]) {
    this.editTableValue = value;

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-info-title', scrollable: true });
  }

  getTime(date: Date): string {
    return this.converter.formatToTime(new Date(date));
  }

  getGermanDate(date: Date): string {
    return this.converter.formatDateToGerman(new Date(date));
  }

  getDate(date: Date): string {
    return this.converter.formatDate(new Date(date));
  }

  getDateTime(value: string): string {
    return this.converter.formatDateToGermanTime(new Date(value));
  }

  isEditableByDate(date: Date) {
    const lastEditDate = new Date();
    lastEditDate.setDate(lastEditDate.getDate() - 14);

    return date > lastEditDate
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
        case this.tableColumns[1]:
          return this.comparer.compareDates(new Date(a[3].start), new Date(b[3].start), isAsc);
        default:
          return 0;
      }
    });
  }
}
