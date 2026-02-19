import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {ServiceService} from "../../../../services/service.service";
import {ServiceDto} from "../../../../dtos/service-dto.model";
import {EmployeeDto} from "../../../../dtos/employee-dto.model";
import {EmployeeService} from "../../../../services/employee.service";
import {Converter} from "../../../../services/converter.helper";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from "@angular/material/core";
import {CsvService} from "../../../../services/csv.service";
import {UnprofessionalDto} from "../../../../dtos/unprofessional-dto.model";
import {AssistancePlan} from "../../../../projections/assistance-plan.projection";

@Component({
    selector: 'app-assistance-plan-evaluation',
    templateUrl: './assistance-plan-evaluation.component.html',
    styleUrls: ['./assistance-plan-evaluation.component.css'],
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
export class AssistancePlanEvaluationComponent implements OnInit, OnChanges {
  @Input() assistancePlan: AssistancePlan = new AssistancePlan();

  // STATES
  isSubmitting = false;

  // CONFIG
  types = [
    'Kategorien/Minuten/Mitarbeiter',
    'Kategorien/Minuten/Fachkräfte',
    'Kategorien/Minuten/keine Fachkräfte',
    'Uhrzeit/Stunden/Kategorien/Inhalt/Mitarbeiter'];

  // VARs
  services: ServiceDto[] = [];
  employees: EmployeeDto[] = [];
  tableSource: MatTableDataSource<any[]> = new MatTableDataSource<any[]>();
  tableColumns: string[] = [];
  selectedType = "";
  selectedDate = new Date(Date.now())

  // FORM
  configForm = new UntypedFormGroup({
    type: new UntypedFormControl(1)
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

  ngOnChanges(changes: SimpleChanges) {
    if (changes['assistancePlan'] && this.assistancePlan.id > 0) {
      this.serviceService.getByAssistancePlan(this.assistancePlan.id).subscribe({
        next: (value) => {
          this.services = value
        }
      });
    }
  }

  loadValues() {
    this.employeeService.allValues$.subscribe(values => this.employees = values);
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
    this.tableSource.data = this.getCategoryMinuteEmployeeRows(this.getProfessionalServices(), this.selectedDate);
  }

  private generateNoProfessionalTable() {
    this.tableColumns = ['Zeitpunkt', 'Kategorien Kürzel', 'Minuten', 'keine Fachkraft'];
    this.tableSource.data = this.getCategoryMinuteEmployeeRows(this.getNoProfessionalServices(), this.selectedDate);
  }

  private generateDetailedTable() {
    this.tableColumns = ['Datum', 'Stunden', 'Kategorien', 'Inhalt', 'Mitarbeiter'];
    this.tableSource.data = this.getTimeHourCategoryContentEmployeeRows(this.services, this.selectedDate);
  }

  private generateEmptyTable() {
    this.tableColumns = [];
    this.tableSource.data = [];
  }

  private getTotalData(): [number, string, number, string][] {
    return this.getCategoryMinuteEmployeeRows(this.services, this.selectedDate);
  }

  private getProfessionalServices(): ServiceDto[] {
  return this.services.filter(service =>
      this.isServiceProfessional(
        service,
        this.assistancePlan.sponsor.id,
        this.getUnProfessionalStates(this.employees)));
  }

  private getNoProfessionalServices(): ServiceDto[] {
    return this.services.filter(service =>
      this.isServiceUnProfessional(
        service,
        this.assistancePlan.sponsor.id,
        this.getUnProfessionalStates(this.employees)
      ));
  }

  private getUnProfessionalStates(employees: EmployeeDto[]): UnprofessionalDto[] {
    return employees.map(employee => employee.unprofessionals).flat();
  }

  private isServiceUnProfessional(service: ServiceDto, sponsorId: number, unprofessionalStates: UnprofessionalDto[]): boolean {
    return !this.isServiceProfessional(service, sponsorId, unprofessionalStates);
  }

  private isServiceProfessional(service: ServiceDto, sponsorId: number, unProfessionalStates: UnprofessionalDto[]): boolean {
    let activeUnProfessionalStates =
      this.getActiveUnProfessionalStates(sponsorId, new Date(service.start), unProfessionalStates);

    return !activeUnProfessionalStates.some(state => state.employeeId == service.employeeId);
  }

  private getActiveUnProfessionalStates(
    sponsorId: number,
    date: Date,
    unProfessionalStates: UnprofessionalDto[]): UnprofessionalDto[] {
    return unProfessionalStates.filter(state => {
      return state.sponsorId == sponsorId &&
        this.isDateAfterDate(state.end, date);
    });
  }

  private isDateAfterDate(check: string | null, target: Date) {
    const checkDate = check != null ? new Date(check) : null;
    checkDate?.setHours(23, 59, 59);

    return ((checkDate != null && checkDate > target) || checkDate == null)
  }

  private getCategoryMinuteEmployeeRows(services: ServiceDto[], targetDate: Date)
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

  private getTimeHourCategoryContentEmployeeRows(services: ServiceDto[], targetDate: Date)
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
