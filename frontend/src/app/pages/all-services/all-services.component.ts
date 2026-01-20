import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {catchError, of, tap} from "rxjs";
import {ReadableInstitutionDto} from "../../shared/dtos/institution-readable-dto.model";
import {InstitutionService} from "../../shared/services/institution.service";
import {ServiceService} from "../../shared/services/service.service";
import {Converter} from "../../shared/services/converter.helper";
import {ActivatedRoute, Router} from "@angular/router";
import {DateService} from "../../shared/services/date.service";
import {ClientsService} from "../../shared/services/clients.service";
import {ClientSoloDto} from "../../shared/dtos/client-solo-dto.model";
import {Service} from "../../shared/dtos/service.projection";
import {EmployeeSolo} from "../../shared/dtos/employee-solo.projection";
import {EmployeeService} from "../../shared/services/employee.service";

@Component({
  selector: 'app-all-services',
  templateUrl: './all-services.component.html',
  styleUrl: './all-services.component.css'
})
export class AllServicesComponent implements OnInit {

  filterDateStart: Date = new Date(Date.now());
  baseUrl: string = "services/all";

  readableInstitutions: ReadableInstitutionDto[] = [];
  clients: ClientSoloDto[] = [];
  employees: EmployeeSolo[] = [];
  title: String = "Meine";

  services: Service[] = [];
  filteredServices: Service[] = [];
  selectedInstitution: ReadableInstitutionDto | null = null;
  selectedClient: ClientSoloDto | null = null;
  selectedEmployee: EmployeeSolo | null = null;
  paramClientId: number | null = null;
  paramInstitutionId: number | null = null;
  paramEmployeeId: number | null = null;
  start: Date = new Date(Date.now());
  end: Date = new Date(Date.now());
  searchString: string = "";
  isBusy = false;
  errorOccurred = true;
  permissionDeniedMessage = "Sie sind nicht berechtigt für diese Anzeige";
  urlClientLoaded = true;
  urlInstitutionLoaded = true;
  urlEmployeeLoaded = true;

  constructor(
    private institutionService: InstitutionService,
    private serviceService: ServiceService,
    private employeeService: EmployeeService,
    private clientService: ClientsService,
    private converter: Converter,
    private dateService: DateService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const start = params.get('start');
      const end = params.get('end');
      const clientIdParam = params.get('clientId');
      let clientId: number = 0;
      if (clientIdParam !== null) {
        const parsedId = Number(clientIdParam);
        if (!isNaN(parsedId)) {
          clientId = parsedId;
        }
      }

      const institutionIdParam = params.get('institutionId');
      let institutionId: number = 0;
      if (institutionIdParam !== null) {
        const parsedId = Number(institutionIdParam);
        if (!isNaN(parsedId)) {
          institutionId = parsedId;
        }
      }

      const employeeIdParam = params.get('employeeId');
      let employeeId: number = 0;
      if (employeeIdParam !== null) {
        const parsedId = Number(employeeIdParam);
        if (!isNaN(parsedId)) {
          employeeId = parsedId;
        }
      }
      if (start === null || start === '0' || end === null || start === '0') {
        this.navigate(this.start, this.end, institutionId, employeeId, clientId);
        return;
      }

      this.paramClientId = clientId;
      this.urlClientLoaded = !(this.paramClientId != null && this.paramClientId > 0);

      this.paramInstitutionId = institutionId;
      this.urlInstitutionLoaded = !(this.paramInstitutionId != null && this.paramInstitutionId > 0);

      this.paramEmployeeId = employeeId;
      this.urlEmployeeLoaded = !(this.paramEmployeeId != null && this.paramEmployeeId > 0);

      this.start = this.dateService.convertDEDateStringToDate(start ?? "") ?? new Date();
      this.end = this.dateService.convertDEDateStringToDate(end ?? "") ?? new Date();
      this.loadReadableInstitutions();
      this.loadClients();
      this.loadEmployees();
      this.initialLoadServices();
    });
  }

  onDateChanged(value: { start: Date, end: Date }) {
    this.start = value.start;
    this.end = value.end;

    this.navigate(this.start, this.end, this.paramInstitutionId ?? 0, this.selectedEmployee?.id ?? 0, this.paramClientId ?? 0);
  }

  onInstitutionChanged(institution: ReadableInstitutionDto | null) {
    this.selectedInstitution = institution;
    this.paramInstitutionId = institution?.id ?? 0;

    if (this.urlInstitutionLoaded) {
      this.navigate(this.start, this.end, this.paramInstitutionId ?? 0, this.paramEmployeeId ?? 0, this.paramClientId ?? 0);
      return;
    }

    this.urlInstitutionLoaded = true;
    this.initialLoadServices();
  }

  onClientChanged(client: ClientSoloDto | null) {
    this.selectedClient = client;
    this.paramClientId = client?.id ?? 0;

    if (this.urlClientLoaded) {
      this.navigate(this.start, this.end, this.paramInstitutionId ?? 0, this.paramEmployeeId ?? 0, this.paramClientId ?? 0);
      return;
    }

    this.urlClientLoaded = true;
    this.initialLoadServices();
  }

  onEmployeeChanged(employee: EmployeeSolo | null) {
    this.selectedEmployee = employee;
    this.paramEmployeeId = employee?.id ?? 0;

    if (this.urlEmployeeLoaded) {
      this.navigate(this.start, this.end, this.paramInstitutionId ?? 0, this.paramEmployeeId ?? 0, this.paramClientId ?? 0);
      return;
    }

    this.urlEmployeeLoaded = true;
    this.initialLoadServices();
  }

  onSearchStringChanged(searchString: string) {
    this.searchString = searchString;
    this.filteredServices = this.services.filter(it => {
      return it.title.toLowerCase().includes(searchString.toLowerCase()) || it.content.toLowerCase().includes(searchString.toLowerCase())
    })
  }

  getDate(date: Date): string {
    return this.converter.formatDate(new Date(date));
  }

  loadReadableInstitutions() {
    this.institutionService
      .getAllReadable()
      .subscribe(values => {
        this.readableInstitutions = values;
        this.cdr.detectChanges();
      })
  }

  loadClients() {
    this.clientService
      .getAllClientSoloDTOs()
      .subscribe(values => {
        this.clients = values;
        this.cdr.detectChanges();
      })
  }

  loadEmployees() {
    this.employeeService
      .getAllProjections()
      .subscribe(values => {
        this.employees = values;
        this.cdr.detectChanges();
      })
  }

  initialLoadServices() {
    if (this.urlInstitutionLoaded && this.urlClientLoaded && this.urlEmployeeLoaded) {
      this.loadServices();
      this.cdr.detectChanges();
    }
  }

  loadServices() {
    this.isBusy = true;
    this.errorOccurred = false;
    this.serviceService
      .getByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(
        this.selectedInstitution?.id ?? 0,
        this.selectedEmployee?.id ?? 0,
        this.selectedClient?.id ?? 0,
        this.start,
        this.end)
      .pipe(
        tap(values => {
            this.services = values;
            this.filteredServices = values;
            this.errorOccurred = false;
            this.isBusy = false;
          this.cdr.detectChanges();
          }),
        catchError(error => {
          this.errorOccurred = true;
          this.isBusy = false;
          this.cdr.detectChanges();

          return of([]);
        })).subscribe();
  }

  private navigate(start: Date, end: Date, institutionId: number, employeeId: number, clientId: number) {
    this.router.navigate([
      this.baseUrl,
      this.dateService.formatDateToYearMonthDay(start),
      this.dateService.formatDateToYearMonthDay(end),
      institutionId,
      employeeId,
      clientId
    ]);
  }
}
