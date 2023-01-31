import {Component, Input, OnInit} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {FormControl, FormGroup} from "@angular/forms";
import {ReplaySubject, switchMap} from "rxjs";
import {AssistancePlanDto} from "../../../dtos/assistance-plan-dto.model";
import {ServiceService} from "../../../services/service.service";
import {ServiceDto} from "../../../dtos/service-dto.model";
import {EmployeeDto} from "../../../dtos/employee-dto.model";
import {EmployeeService} from "../../../services/employee.service";
import {Converter} from "../../converter.helper";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {CsvService} from "../../../services/csv.service";

@Component({
  selector: 'app-assistance-plan-evaluation',
  templateUrl: './assistance-plan-evaluation.component.html',
  styleUrls: ['./assistance-plan-evaluation.component.css'],
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
export class AssistancePlanEvaluationComponent implements OnInit {
  @Input() assistancePlan$ = new ReplaySubject<AssistancePlanDto>();

  // STATES
  isSubmitting = false;

  // CONFIG
  types = [
    'Kategorien/Minuten/Mitarbeiter',
    'Kategorien/Minuten/Fachkräfte',
    'Kategorien/Minuten/keine Fachkräfte',
    'Uhrzeit/Stunden/Kategorien/Inhalt/Mitarbeiter'];

  // VARs
  assistancePlan: AssistancePlanDto = new AssistancePlanDto();
  services: ServiceDto[] = [];
  employees: EmployeeDto[] = [];
  tableSource: MatTableDataSource<any[]> = new MatTableDataSource<any[]>();
  tableColumns: string[] = [];
  selectedType = "";
  selectedDate = new Date(Date.now())

  // FORM
  configForm = new FormGroup({
    type: new FormControl(1)
  });

  get typeControl() { return this.configForm.controls['type']; }

  constructor(
    private serviceService: ServiceService,
    private employeeService: EmployeeService,
    private csvService: CsvService,
    private converter: Converter
  ) { }

  ngOnInit(): void {
    this.loadValues();
    this.initFormSubscriptions();
  }

  loadValues() {
    this.employeeService.allValues$.subscribe(values => this.employees = values);

    this.assistancePlan$
      .pipe(switchMap(value => {
        this.assistancePlan = value;
        return this.serviceService.getByAssistancePlan(value.id);
      }))
      .subscribe(value => {
        this.services = value;
      });
  }

  initFormSubscriptions() {
    this.typeControl.valueChanges.subscribe(value => {
      this.selectedType = value;
      this.generateTable();
    });
  }

  generateTableType(type: string) {
    this.selectedType = type;
    this.generateTable();
  }

  increaseMonth() {
    this.selectedDate = new Date(this.selectedDate.setMonth(this.selectedDate.getMonth() + 1));
    this.generateTable();
  }

  decreaseMonth() {
    this.selectedDate = new Date(this.selectedDate.setMonth(this.selectedDate.getMonth() - 1));
    this.generateTable();
  }

  exportToCSV() {
    this.csvService.exportToCsv(`hilfeplan_${this.assistancePlan.id}.csv`, this.tableSource.data);
  }

  generateTable() {
    switch (this.selectedType) {
      case this.types[0]:
        this.generateTotalTable();
        break;
      case this.types[1]:
        this.generateProfessionalTable();
        break;
      case this.types[2]:
        this.generateNoProfessionalTable();
        break;
      case this.types[3]:
        this.generateDetailedTable();
        break;
      default:
        this.generateEmptyTable();
        break;
    }
  }

  private generateTotalTable() {
    this.tableColumns = ['Zeitpunkt', 'Kategorien Kürzel', 'Minuten', 'Mitarbeiter'];
    this.tableSource.data = this.getTotalData();
  }

  private generateProfessionalTable() {
    this.tableColumns = ['Zeitpunkt', 'Kategorien Kürzel', 'Minuten', 'Fachkraft'];
    this.tableSource.data = this.getKMMRows(this.getProfessionalServices(), this.selectedDate);
  }

  private generateNoProfessionalTable() {
    this.tableColumns = ['Zeitpunkt', 'Kategorien Kürzel', 'Minuten', 'keine Fachkraft'];
    this.tableSource.data = this.getKMMRows(this.getNoProfessionalServices(), this.selectedDate);
  }

  private generateDetailedTable() {
    this.tableColumns = ['Datum', 'Stunden', 'Kategorien', 'Inhalt', 'Mitarbeiter'];
    this.tableSource.data = this.getTHKCMRows(this.services, this.selectedDate);
  }

  private generateEmptyTable() {
    this.tableColumns = [];
    this.tableSource.data = [];
  }

  private getTotalData(): [number, string, number, string][] {
    return this.getKMMRows(this.services, this.selectedDate);
  }

  private getProfessionalServices(): ServiceDto[] {
    return this.services.filter(service => !this.isProfessionalService(service, this.employees));
  }

  private getNoProfessionalServices(): ServiceDto[] {
    return this.services.filter(service => !this.isProfessionalService(service, this.employees));
  }

  private isProfessionalService(service: ServiceDto, employees: EmployeeDto[]): boolean {
    const noProfessionals = employees.find(employee => employee.id == service.employeeId)?.unprofessionals ?? [];

    return !noProfessionals
      .some(noProfessional => {
        const endDate = noProfessional.end != null ? new Date(noProfessional.end) : null
        endDate?.setHours(23, 59, 59);

        return noProfessional.sponsorId == this.assistancePlan.sponsorId &&
          ((endDate != null && endDate > new Date(service.start)) || endDate == null)
    });
  }

  private getKMMRows(services: ServiceDto[], targetDate: Date)
    : [number, string, number, string][] {
    const rows = Array<[number, string, number, string]>(this.converter.getDaysOfMonth(targetDate));

    // rows with day
    for (let i = 0; i < rows.length; i++) {
      rows[i] = [i+1, "", 0, ""];
    }

    services.forEach(value => {
      const date = new Date(value.start);

      if (date.getMonth() != targetDate.getMonth() || date.getFullYear() != targetDate.getFullYear())
        return;

      const day = date.getDate();
      const employee = this.employees.find(employee => value.employeeId == employee.id) ?? new EmployeeDto();
      const shortCut = this.converter.getInitials(employee.firstName + " " + employee.lastName, 2);

      // category shortcuts
      value.categorys.forEach(category =>
        rows[day - 1][1] = this.converter.concatStringIfNotExists(rows[day - 1][1], category.shortcut, ","));
      // minutes
      rows[day - 1][2] += value.minutes;
      // employee shortcut
      rows[day - 1][3] = this.converter.concatStringIfNotExists(rows[day - 1][3], shortCut, ",");
    });

    return rows;
  }

  private getTHKCMRows(services: ServiceDto[], targetDate: Date)
    : [string, number, string, string, string][] {
    return services
      .filter(value => {
        const start = new Date(value.start);
        return start.getMonth() == targetDate.getMonth() && start.getFullYear() == targetDate.getFullYear();
      })
      .sort((a,b) => (new Date(a.start)).getTime() - (new Date(b.start)).getTime())
      .map(value => {
        let row: [string, number, string, string, string] = ["", 0, "", "", ""];
        const start = new Date(value.start);
        const end = new Date(value.end);
        const employee = this.employees.find(employee => value.employeeId == employee.id) ?? new EmployeeDto();

        // date
        row[0] = start.toLocaleDateString("de-DE") + "\n" +
          start.toLocaleTimeString("de-DE") + " - " + end.toLocaleTimeString("de-DE");
        row[1] = this.converter.roundTo2Digits(value.minutes / 60.0);
        value.categorys.forEach(category =>
          row[2] = this.converter.concatStringIfNotExists(row[2], category.title, ","));
        row[3] = ((value.title.length > 0) ? value.title + "\n\n" : "") + value.content;
        row[4] = employee.firstName + " " + employee.lastName;

        return row;
    });
  }
}
