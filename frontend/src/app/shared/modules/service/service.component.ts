import {Component, Input, OnInit} from '@angular/core';
import {TablePageComponent} from "../table-page.component";
import {ServiceDto} from "../../../dtos/service-dto.model";
import {Sort} from "@angular/material/sort";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ServiceService} from "../../../services/service.service";
import {UserService} from "../../../services/user.service";
import {ReplaySubject} from "rxjs";
import {Converter} from "../../converter.helper";
import {ClientDto} from "../../../dtos/client-dto.model";
import {ClientsService} from "../../../services/clients.service";
import {combineLatest} from "rxjs";
import {HelperService} from "../../../services/helper.service";
import {InstitutionDto} from "../../../dtos/institution-dto.model";
import {InstitutionService} from "../../../services/institution.service";
import {EmployeeDto} from "../../../dtos/employee-dto.model";
import {EmployeeService} from "../../../services/employee.service";

@Component({
  selector: 'app-service',
  templateUrl: './service.component.html',
  styleUrls: ['./service.component.css']
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

  // FILTER-VARs
  filterDate: Date = new Date(Date.now());

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private converter: Converter,
    private serviceService: ServiceService,
    private clientService: ClientsService,
    private institutionService: InstitutionService,
    private employeeService: EmployeeService,
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

    if (this.clientId != null) {
      this.loadServicesByClient();
    }

    if (this.employeeId != null) {
      this.loadServicesByEmployee();
    }
  }

  loadServicesByEmployee() {
    combineLatest([
      this.serviceService.getByEmployeeAndDate(this.employeeId ?? 0, this.filterDate),
      this.employeeId$
    ])
      .subscribe({
        next: ([services, employeeId]) => {
          this.employeeId = employeeId;
          this.values$.next(services);
          this.values = services;
          this.isSubmitting = false;

          this.refreshTableData();
        },
        error: () => this.handleFailure("Fehler beim laden")
      });
  }

  loadServicesByClient() {
    combineLatest([
      this.serviceService.getByClientAndDate(this.clientId ?? 0, this.filterDate),
      this.clientId$
    ])
      .subscribe({
        next: ([services, client]) => {
          this.clientId = client;
          this.values$.next(services);
          this.values = services;
          this.isSubmitting = false;

          this.refreshTableData();
        },
        error: () => this.handleFailure("Fehler beim laden")
      });
  }

  refreshTableData() {
    this.sourceTableData = this.getTableData();
    this.filteredTableData = this.sourceTableData;
    this.refreshTablePage();
  }

  getTableData(): [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean][] {
    return this.values
      .map(value => [
        this.clients.find(client => client.id == value.clientId) ?? new ClientDto(),
        this.employees.find(employee => employee.id == value.employeeId) ?? new EmployeeDto(),
        this.institutions.find(institution => institution.id == value.institutionId) ?? new InstitutionDto(),
        value,
        (this.user.access?.role ?? 99) == 1 || value.employeeId == this.user.id
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

  filterTableData() {
  }

  getNewValue(): ServiceDto {
    return new ServiceDto();
  }

  initFormSubscriptions() {
    // no form is in use
  }

  handleFilterDateChanged() {
    this.loadValues();
  }

  increaseFilterDate(days: number) {
    this.filterDate.setDate(this.filterDate.getDate() + days);
    this.loadValues();
  }

  openServiceInformationModal(content, value: [ClientDto, EmployeeDto, InstitutionDto, ServiceDto, boolean]) {
    this.editTableValue = value;

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-info-title', scrollable: true });
  }

  getTime(date: Date): string {
    return this.converter.formatToTime(new Date(date));
  }

  getDate(date: Date): string {
    return this.converter.formatDateToGerman(new Date(date));
  }

  getDateTime(value: string): string {
    return this.converter.formatDateToGermanTime(new Date(value));
  }

  sortData(sort: Sort) {
    // TODO: sort table
  }
}
