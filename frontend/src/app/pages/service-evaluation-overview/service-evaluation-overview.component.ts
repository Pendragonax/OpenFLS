import {Component, OnInit} from '@angular/core';
import {combineLatest, ReplaySubject} from "rxjs";
import {ActivatedRoute} from "@angular/router";
import {FormControl, FormGroup} from "@angular/forms";
import {OverviewService} from "../../services/overview.service";
import {OverviewAssistancePlan} from "../../dtos/overview-assistance-plan.dto";
import {Location} from '@angular/common';
import {HourTypeDto} from "../../dtos/hour-type-dto.model";
import {HourTypeService} from "../../services/hour-type.service";
import {InstitutionService} from "../../services/institution.service";
import {InstitutionDto} from "../../dtos/institution-dto.model";
import {SponsorService} from "../../services/sponsor.service";
import {SponsorDto} from "../../dtos/sponsor-dto.model";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from "@angular/material/core";
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MAT_MOMENT_DATE_FORMATS,
  MomentDateAdapter
} from "@angular/material-moment-adapter";
import {EOverviewType} from "../../enums/EOverviewType";
import {Converter} from "../../shared/converter.helper";

@Component({
  selector: 'app-service-evaluation-overview',
  templateUrl: './service-evaluation-overview.component.html',
  styleUrls: ['./service-evaluation-overview.component.css'],
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
export class ServiceEvaluationOverviewComponent implements OnInit {
  readonly FIXED_COLUMN_FROM_INDEX: number = 2
  readonly COMBINATION_COLUMN_NAME: string = "Gesamt"

  clientColumnHeader = "Klient"
  assistancePlanColumnHeader = "Ende"
  columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>()
  data$: ReplaySubject<string[][]> = new ReplaySubject()
  columnFixedWidthFromIndex$: ReplaySubject<number> = new ReplaySubject<number>()

  columns: string[] = []
  data: string[][] = []
  columnFixedWidthFromIndex: number = 0

  selectedPeriodMode: number = 1;
  hourTypeAll = new HourTypeDto({title:"alle"})
  hourTypes: HourTypeDto[] = []
  selectedHourType: HourTypeDto | null = null;
  areaAll = new InstitutionDto({name:"alle"})
  areas: InstitutionDto[] = [this.areaAll]
  selectedArea: InstitutionDto | null = null;
  sponsorAll = new SponsorDto({name:"alle"})
  sponsors: SponsorDto[] = [this.sponsorAll]
  selectedSponsor: SponsorDto | null = null;
  valueTypes: string[] = Object.values(EOverviewType);
  selectedValueType: EOverviewType = EOverviewType.EXECUTED_HOURS;
  year: number = new Date().getFullYear() + 1;
  month: number = 0;
  outputString: string = "";
  generationAllowed: boolean = false;

  // Status
  isGenerating: boolean = false;

  selectionForm: FormGroup = new FormGroup({
    periodModeControl: new FormControl({value: '2', disabled: this.isGenerating}),
    hourTypeControl: new FormControl(),
    areaControl: new FormControl(),
    sponsorControl: new FormControl(),
    valueTypeControl: new FormControl()
  });


  constructor(private route: ActivatedRoute,
              private overviewService: OverviewService,
              private hourTypeService: HourTypeService,
              private institutionService: InstitutionService,
              private sponsorService: SponsorService,
              private converter: Converter,
              private location: Location) { }

  get periodModeControl() { return this.selectionForm.controls['periodModeControl']; }
  get hourTypeControl() { return this.selectionForm.controls['hourTypeControl']; }
  get areaControl() { return this.selectionForm.controls['areaControl']; }
  get sponsorControl() { return this.selectionForm.controls['sponsorControl']; }
  get valueTypeControl() { return this.selectionForm.controls['valueTypeControl']; }

  ngOnInit(): void {
    this.isGenerating = true;
    this.initFormControlSubscriptions();
    this.loadValues();
  }

  loadValues() {
    combineLatest([
      this.hourTypeService.allValues$,
      this.institutionService.allValues$,
      this.sponsorService.allValues$
    ]).subscribe(([hourTypes, institutions, sponsors]) => {
      this.hourTypes = []
      this.hourTypes.push(...hourTypes);
      this.areas = [this.areaAll]
      this.areas.push(...institutions);
      this.sponsors = [this.sponsorAll]
      this.sponsors.push(...sponsors);
      this.valueTypes = Object.values(EOverviewType);
      this.columnFixedWidthFromIndex$.next(this.FIXED_COLUMN_FROM_INDEX);
      this.executeURLParams();
    })
  }

  initFormControlSubscriptions() {
    this.periodModeControl.valueChanges.subscribe(value => {
      if (this.month == 0) {
        this.month = new Date().getMonth() + 1
      }

      this.selectedPeriodMode = value;
      this.updateUrl();
      this.validateGenerationStatus();
    });
    this.hourTypeControl.valueChanges.subscribe(value => {
      this.selectedHourType = this.hourTypes.find(it => it.id == value) ?? null;
      this.updateUrl();
      this.validateGenerationStatus();
    });
    this.areaControl.valueChanges.subscribe(value => {
      this.selectedArea = this.areas.find(it => it.id == value) ?? null;
      this.updateUrl();
      this.validateGenerationStatus();
    });
    this.sponsorControl.valueChanges.subscribe(value => {
      this.selectedSponsor = this.sponsors.find(it => it.id == value) ?? null;
      this.validateGenerationStatus();
    });
    this.valueTypeControl.valueChanges.subscribe(value => {
      this.selectedValueType = this.getEnumByValue(EOverviewType, value) ?? EOverviewType.EXECUTED_HOURS;
      this.updateUrl();
      this.validateGenerationStatus();
    });
  }

  executeURLParams() {
    this.route.params.subscribe(params => {
      this.periodModeControl.setValue(params['month'] != null && params['month'] != '0' ? '2' : '1');
      this.year = params['year'] != null ? +params['year'] : 2023;

      if (params['month'] != null) {
        this.month = params['month'] <= 0 || params['month'] > 12 ? 1 : +params['month'];
      }
      this.hourTypeControl.setValue(this.hourTypes.find(value => value.id == params['hourTypeId'])?.id);
      this.areaControl.setValue(this.areas.find(value => value.id == params['areaId'])?.id);
      this.sponsorControl.setValue(this.sponsors.find(value => value.id == params['sponsorId'])?.id);
      this.valueTypeControl.setValue(this.valueTypes.find(value => value == params['valueTypeId']));

      this.isGenerating = false;
    });
  }

  nextYear() {
    this.year += 1;
    this.updateUrl();
  }

  prevYear() {
    this.year -= 1;
    this.updateUrl();
  }

  nextMonth() {
    if (this.month == 12) {
      this.month = 1;
      this.year += 1;
    } else {
      this.month += 1;
    }
    this.updateUrl();
  }

  prevMonth() {
    if (this.month == 1) {
      this.month = 12;
      this.year -= 1;
    } else {
      this.month -= 1;
    }
    this.updateUrl();
  }

  private validateGenerationStatus() {
    this.generationAllowed =
      this.selectedValueType != null &&
      this.selectedSponsor != null &&
      this.selectedArea != null &&
      this.selectedHourType != null &&
      this.selectedPeriodMode != null;
  }

  generateTable() {
    this.isGenerating = true;

    if (this.selectedPeriodMode == 1) {
      this.overviewService
        .getOverviewFromAssistancePlanByYear(this.year,this.selectedHourType?.id ?? null, this.selectedArea?.id ?? null,this.selectedSponsor?.id ?? null, this.selectedValueType)
        .subscribe({
          next: (value) => {
            this.columns = this.getMonthsInYearColumns()
            this.columns$.next(this.columns);
            this.data = this.convertToData(value);
            this.data$.next(this.data);
            this.isGenerating = false;
          }
        })
    } else {
      this.overviewService
        .getOverviewFromAssistancePlanByYearAndMonth(this.year, this.month,this.selectedHourType?.id ?? null, this.selectedArea?.id ?? null,this.selectedSponsor?.id ?? null, this.selectedValueType)
        .subscribe({
          next: (value) => {
            this.columns = this.getDaysInMonthColumns(this.year, this.month);
            this.columns$.next(this.columns);
            this.data = this.convertToData(value);
            this.data$.next(this.data);
            this.isGenerating = false;
          }
        })
    }
  }

  private convertToData(source: OverviewAssistancePlan[]) {
    return source.map(value => {
      // client name
      let result = [(value.clientDto?.lastName ?? "") + " " + (value.clientDto?.firstName ?? "unbekannt")];

      // assistance plan end
      result.push(this.getDateString(value.assistancePlanDto?.end ?? null));

      console.log("Number of rows = " + value.values.length)
      for (let i = 0; i < value.values.length; i++) {
        result.push(value.values[i].toString());
      }
      return result;
    });
  }

  getMonthName(month: number): string {
    const date = new Date();
    date.setMonth(month - 1);
    return date.toLocaleString('de-DE', { month: 'long' });
  }

  private getDaysInMonth(year: number, month: number): number {
    // Check if the month is valid (1 to 12)
    if (month < 1 || month > 12) {
      throw new Error("Month must be between 1 and 12");
    }

    // Use the Date class to calculate the number of days
    return new Date(year, month, 0).getDate();
  }

  private getDaysInMonthColumns(year: number, month: number): string[] {
    const daysInMonth = this.getDaysInMonth(year, month);
    let daysArray: string[] = [this.clientColumnHeader, this.assistancePlanColumnHeader];

    daysArray.push(this.COMBINATION_COLUMN_NAME)
    for (let i = 1; i <= daysInMonth; i++) {
      daysArray.push(i.toString().padStart(2, '0'));
    }

    return daysArray;
  }

  private getMonthsInYearColumns(): string[] {
    let daysArray: string[] = [this.clientColumnHeader, this.assistancePlanColumnHeader];

    daysArray.push(this.COMBINATION_COLUMN_NAME)
    for (let i = 1; i <= 12; i++) {
      daysArray.push(i.toString().padStart(2, '0'));
    }

    return daysArray;
  }

  private updateUrl() {
    let monthParam = this.selectedPeriodMode == 1 ? 0 : this.month
    this.location.go(`overview/${this.year}/${monthParam}/${this.selectedHourType?.id}/${this.selectedArea?.id}/${this.selectedSponsor?.id}/${this.selectedValueType}`);
  }

  getDateString(dateString: string | null): string {
    return this.converter.getLocalDateString(dateString);
  }

  private getEnumByValue<T>(enumObj: T, value: T[keyof T]): T[keyof T] | null {
    for (const key in enumObj) {
      if (enumObj[key] === value) {
        return enumObj[key] as T[keyof T];
      }
    }
    return null;
  }
}
