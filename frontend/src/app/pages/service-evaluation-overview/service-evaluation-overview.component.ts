import { Component, OnInit } from '@angular/core';
import { ReplaySubject} from "rxjs";
import {ActivatedRoute} from "@angular/router";
import {FormControl, FormGroup} from "@angular/forms";
import {OverviewService} from "../../services/overview.service";
import {OverviewAssistancePlan} from "../../dtos/overview-assistance-plan.dto";

@Component({
  selector: 'app-service-evaluation-overview',
  templateUrl: './service-evaluation-overview.component.html',
  styleUrls: ['./service-evaluation-overview.component.css']
})
export class ServiceEvaluationOverviewComponent implements OnInit {
  columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>();
  data$: ReplaySubject<string[][]> = new ReplaySubject();

  columns: string[] = []
  data: string[][] = []

  selectedPeriodMode: number = 1;
  hourTypes: string[] = ["alle", "qualifizierte Assistenz", "kompensatorische Assistenz"]
  selectedHourType = "";
  overviewMode: string[] = ["Klient", "Hilfeplan"]
  selectedOverviewMode: string = "";
  areas: string[] = ["Betreutes Wohnen", "Intensiv Betreutes Wohnen"]
  selectedArea: string = "";
  sponsors: string[] = ["LWV", "LWL"]
  selectedSponsor: string = "";
  valueTypes: string[] = ["geleistete Stunden", "beantragte Stunden", "Differenz"]
  selectedValueType: string = "";
  year: number = 2023;
  month: number = 1;
  outputString: string = "";
  generationAllowed: boolean = false;

  selectionForm: FormGroup = new FormGroup({
    periodModeControl: new FormControl('1'),
    overviewModeControl: new FormControl(),
    hourTypeControl: new FormControl(),
    areaControl: new FormControl(),
    sponsorControl: new FormControl(),
    valueTypeControl: new FormControl()
  });

  // Status
  isGenerating: boolean = false;

  constructor(private route: ActivatedRoute,
              private overviewService: OverviewService) { }

  get periodModeControl() { return this.selectionForm.controls['periodModeControl']; }
  get overviewModeControl() { return this.selectionForm.controls['overviewModeControl']; }
  get hourTypeControl() { return this.selectionForm.controls['hourTypeControl']; }
  get areaControl() { return this.selectionForm.controls['areaControl']; }
  get sponsorControl() { return this.selectionForm.controls['sponsorControl']; }
  get valueTypeControl() { return this.selectionForm.controls['valueTypeControl']; }

  ngOnInit(): void {
    this.initFormControlSubscriptions();
    this.executeURLParams();
  }

  initFormControlSubscriptions() {
    this.periodModeControl.valueChanges.subscribe(value => {
      this.selectedPeriodMode = value;
      this.validateGenerationStatus();
    });
    this.overviewModeControl.valueChanges.subscribe(value => {
      this.selectedOverviewMode = value;
      this.validateGenerationStatus();
    });
    this.hourTypeControl.valueChanges.subscribe(value => {
      this.selectedHourType = value;
      this.validateGenerationStatus();
    });
    this.areaControl.valueChanges.subscribe(value => {
      this.selectedArea = value;
      this.validateGenerationStatus();
    });
    this.sponsorControl.valueChanges.subscribe(value => {
      this.selectedSponsor = value;
      this.validateGenerationStatus();
    });
    this.valueTypeControl.valueChanges.subscribe(value => {
      this.selectedValueType = value;
      this.validateGenerationStatus();
    });
  }

  executeURLParams() {
    this.route.params.subscribe(params => {
      this.periodModeControl.setValue(params['month'] != '0' ? '2' : '1');
      this.year = params['year'] != null ? params['year'] : 2023;

      if (params['month'] != null) {
        this.month = params['month'] <= 0 || params['month'] > 12 ? 1 : params['month'];
      }
      this.overviewModeControl.setValue(this.overviewMode[params['overviewModeId']]);
      this.hourTypeControl.setValue(this.hourTypes[params['hourTypeId']]);
      this.areaControl.setValue(this.areas[params['areaId']]);
      this.sponsorControl.setValue(this.sponsors[params['sponsorId']]);
      this.valueTypeControl.setValue(this.valueTypes[params['valueTypeId']]);
    });
  }

  nextYear() {
    this.year += 1;
  }

  prevYear() {
    this.year -= 1;
  }

  nextMonth() {
    if (this.month == 12) {
      this.month = 1;
      this.year += 1;
    } else {
      this.month += 1;
    }
  }

  prevMonth() {
    if (this.month == 1) {
      this.month = 12;
      this.year -= 1;
    } else {
      this.month -= 1;
    }
  }

  validateGenerationStatus() {
    this.generationAllowed =
      this.selectedValueType != null &&
      this.selectedOverviewMode != null &&
      this.selectedSponsor != null &&
      this.selectedArea != null &&
      this.selectedHourType != null &&
      this.selectedPeriodMode != null;
  }

  getMonthName(month: number): string {
    const date = new Date();
    date.setMonth(month - 1);
    return date.toLocaleString('de-DE', { month: 'long' });
  }

  generateTable() {
    this.isGenerating = true;
    this.overviewService
      .getExecutedHoursOverviewFromAssistancePlanByYearAndMonth(this.year,2,2,1)
      .subscribe({
        next: (value) => {
          this.columns = ["Name", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"]
          this.columns$.next(this.columns);
          this.data = this.convertToData(value);
          this.data$.next(this.data);
          console.log(value);
          this.isGenerating = false;
        }
      })
    // let count = this.selectedPeriodMode == 1 ? 13 : 32;
    // this.columns = [];
    // this.data = [];
    //
    // for (let i = 0; i < count; i++) {
    //   if (i == 0) {
    //     this.columns.push("Name");
    //   } else {
    //     this.columns.push(i.toString());
    //   }
    // }
    //
    // for (let amount = 0; amount < 30; amount++) {
    //   let data: string[] = [];
    //   for (let i = 0; i < this.columns.length; i++) {
    //     if (i == 0) {
    //       data.push("unbekannt");
    //     } else {
    //       data.push((amount + 1) + "|" + i);
    //     }
    //   }
    //   this.data.push(data);
    // }
    //
    // this.columns$.next(this.columns);
    // this.data$.next(this.data);
    // this.isGenerating = false;
  }

  convertToData(source: OverviewAssistancePlan[]) {
    return source.map(value => [value.clientDto?.firstName ?? "unbekannt",
      value.values[0].toString(),
      value.values[1].toString(),
      value.values[2].toString(),
      value.values[3].toString(),
      value.values[4].toString(),
      value.values[5].toString(),
      value.values[6].toString(),
      value.values[7].toString(),
      value.values[8].toString(),
      value.values[9].toString(),
      value.values[10].toString(),
      value.values[11].toString()]);
  }
}
