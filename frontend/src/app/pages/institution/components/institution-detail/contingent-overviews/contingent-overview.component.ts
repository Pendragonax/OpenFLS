import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {ContingentEvaluationDto} from "./dtos/contingent-evaluation-dto.model";
import {ContingentEvaluationService} from "./services/contingent-evaluation.service";
import {MatDialog} from "@angular/material/dialog";
import {InformationModal} from "../../../../../shared/modals/information/information.modal";
import {CsvService} from "../../../../../shared/services/csv.service";

@Component({
    selector: 'app-contingent-overview',
    templateUrl: './contingent-overview.component.html',
    styleUrls: ['./contingent-overview.component.css'],
    standalone: false
})
export class ContingentOverviewComponent implements OnInit {

  @Input() institutionId: number = 0

  informationTitle: string = "Kontingentübersicht Informationen"
  informationContent: {title: string, content: string}[] = [
    {
      title: "Kontingent [h]",
      content: "Die Zeitangabe beschreibt den optimalen Monatswert ohne Krankheit, Fortbildung oder Urlaub.<br>Der Gesamtwert ist dabei bereinigt und umfasst nur die vorausgesetzten 195 tatsächliche Arbeitstage. <br>Aus diesem Grund beschreibt dieser nicht die Summe aller optimalen Einzelmonate, sondern fällt geringer aus durch das Abziehen der Abwesenheitstage."
    },
    {
      title: "Geleistet [%]",
      content: "Der Wert beschreibt die Prozent vom optimalen Monatswert. Dabei ist zu beachten, dass für einen Abwesenheitstag ~5% abzuziehen sind.<br>Der Gesamtprozentwert ist dabei wieder korrekt bereinigt und umfasst das komplette Jahr mit den vorausgesetzten 195 tatsächliche Arbeitstagen."
    },
    {
      title: "Geleistet summiert [%]",
      content: "Der Wert beschreibt die Prozent vom bereinigten Monatswert. Dabei werden alle vorangegangenen Monate jeweils mit einbezogen.<br>Der Gesamtwert Prozentwert ist dabei wieder bereinigt und umfasst das komplette Jahr mit den vorausgesetzten 195 tatsächliche Arbeitstagen."
    }]

  header$: ReplaySubject<string[]> = new ReplaySubject()
  data$: ReplaySubject<any[][]> = new ReplaySubject()
  tableHeader: string[] = []
  tableData: any[][] = []

  isGenerating: boolean = false
  isLoadedInitially: boolean = false
  errorOccurred: boolean = false
  contingentOverView: ContingentEvaluationDto | null = null
  year: number = new Date(Date.now()).getFullYear()
  years: number[] = []
  hourTypes: { name: string, value: number }[] =
    [{name: "Kontingent [h]", value: 1}, {name: "Geleistet [h]", value: 2}, {name: "Geleistet [%]", value: 3}, {name: "Geleistet summiert [%]", value: 4}, {name: "Nicht geleistet [h]", value: 5}]
  selectedHourType: number = 0

  constructor(private contingentEvaluationService: ContingentEvaluationService,
              private dialog: MatDialog,
              private csvService: CsvService) { }

  ngOnInit(): void {
    this.years = this.buildYearOptions()
    this.getHeader()
    this.selectedHourType = this.hourTypes[0]?.value ?? 0
    this.loadValues()
  }

  onYearChanged(year: number) {
    this.year = year
    this.loadValues()
  }

  onHourTypeChanged(value: number) {
    this.selectedHourType = value
    this.isLoadedInitially = this.contingentOverView != null
    this.getData()
  }

  public loadValues() {
    this.isGenerating = true
    this.errorOccurred = false

    this.contingentEvaluationService.getOverviewByInstitutionIdAndYear(this.institutionId, this.year).subscribe({
      next: value => {
        this.contingentOverView = value
        this.isLoadedInitially = this.selectedHourType > 0
        this.getData()
        this.isGenerating = false
      },
      error: _ => {
        this.isGenerating = false
        this.errorOccurred = true
      }
    })
  }

  openValueTypeInfoModal(event) {
    event.stopPropagation();
    this.dialog.open(InformationModal, {
      data: {
        title: this.informationTitle,
        content: this.informationContent
      }
    })
  }

  public getHeader() {
    this.tableHeader = this.getYearHeader()
    this.header$.next(this.tableHeader)
  }

  public getData() {
    let result: any[] = []

    if (this.contingentOverView != null && this.selectedHourType > 0) {
      for (let employee of this.contingentOverView.employees) {
        let employeeRow: any[] = []
        employeeRow.push(employee.lastname + ", " + employee.firstname)
        let values: number[]
        switch (this.selectedHourType) {
          case 1: {
            values = employee.contingentHours
            break
          }
          case 2: {
            values = employee.executedHours
            break
          }
          case 3: {
            values = employee.executedPercent
            break
          }
          case 4: {
            values = employee.summedExecutedPercent
            break
          }
          case 5: {
            values = employee.missingHours
            break
          }
          default: {
            values = employee.contingentHours
            break
          }
        }

        for (let value of values) {
          employeeRow.push(value)
        }

        result.push(employeeRow)
      }
    }

    this.tableData = result
    this.data$.next(result)
  }

  exportTable() {
    if (this.tableData.length === 0) {
      return
    }
    this.csvService.exportToCsvWithHeader("ContingentOverview", this.tableData, this.tableHeader)
  }

  private getYearHeader(): string[] {
    let result: string[] = []
    result.push("Name")
    result.push("Gesamt")
    for (let col = 1; col < 13; col++) {
      result.push(col.toString())
    }
    return result
  }

  private buildYearOptions(): number[] {
    const currentYear = new Date(Date.now()).getFullYear()
    return Array.from({length: 11}, (_, index) => currentYear - index)
  }

}
