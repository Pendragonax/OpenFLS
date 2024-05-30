import {Component, OnInit} from '@angular/core';
import {combineLatest, Observable, ReplaySubject} from "rxjs";
import {ActivatedRoute} from "@angular/router";
import {UntypedFormControl, UntypedFormGroup} from "@angular/forms";
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
import {
  OverviewValueTypeInfoModalComponent
} from "../../modals/overview-valuetype-info-modal/overview-value-type-info-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {HttpErrorResponse, HttpStatusCode} from "@angular/common/http";
import {
  OverviewPermissionInfoModalComponent
} from "../../modals/overview-permission-info-modal/overview-permission-info-modal.component";
import {DateService} from "../../services/date.service";
import {
  AssistancePlanAnalysisService
} from "../../domains/assistance-plan-analysis/services/assistance-plan-analysis.service";
import {
  AssistancePlansAnalysisMonthDto
} from "../../domains/assistance-plan-analysis/dtos/assistance-plans-analysis-month-dto";

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
  readonly CLIENT_COLUMN_HEADER: string = "Klient"
  readonly ASSISTANCE_PLAN_START_COLUMN_HEADER: string = "Start"
  readonly ASSISTANCE_PLAN_END_COLUMN_HEADER: string = "Ende"
  readonly PERIOD_MODE_YEARLY: number = 1
  readonly PERIOD_MODE_MONTHLY: number = 2

  columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>()
  data$: ReplaySubject<string[][]> = new ReplaySubject()
  columnFixedWidthFromIndex$: ReplaySubject<number> = new ReplaySubject<number>()
  boldColumnIndices$: ReplaySubject<number[]> = new ReplaySubject<number[]>()

  columns: string[] = []
  data: string[][] = []
  columnFixedWidthFromIndex: number = 0
  boldColumnIndices: number[] = [2]

  selectedPeriodMode: number = this.PERIOD_MODE_YEARLY;
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
  selectedValueType: EOverviewType | null = null;
  year: number = new Date().getFullYear() + 1;
  month: number = 0;
  outputString: string = "";
  generationAllowed: boolean = false;

  // Status
  isGenerating: boolean = false;
  forbiddenRequest: boolean = false;
  errorOccurred: boolean = false;
  csvGenerated: boolean = false;

  selectionForm: UntypedFormGroup = new UntypedFormGroup({
    periodModeControl: new UntypedFormControl({value: '2', disabled: this.isGenerating}),
    hourTypeControl: new UntypedFormControl({disabled: this.isGenerating}),
    areaControl: new UntypedFormControl({disabled: this.isGenerating}),
    sponsorControl: new UntypedFormControl({disabled: this.isGenerating}),
    valueTypeControl: new UntypedFormControl({disabled: this.isGenerating})
  });

  constructor(private route: ActivatedRoute,
              private overviewService: OverviewService,
              private hourTypeService: HourTypeService,
              private institutionService: InstitutionService,
              private sponsorService: SponsorService,
              private dateService: DateService,
              private assistancePlanAnalysisService: AssistancePlanAnalysisService,
              private converter: Converter,
              private dialog: MatDialog,
              private location: Location) {
    this.boldColumnIndices$.next(this.boldColumnIndices)
  }

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
      this.loadURLParams();
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
      this.updateUrl();
      this.validateGenerationStatus();
    });

    this.valueTypeControl.valueChanges.subscribe(value => {
      this.selectedValueType = this.getEnumByValue(EOverviewType, value) ?? null;
      this.updateUrl();
      this.validateGenerationStatus();
    });
  }

  loadURLParams() {
    this.route.params.subscribe(params => {
      this.periodModeControl.setValue(params['month'] != null && params['month'] != '0' ? '2' : '1');
      this.year = params['year'] != null ? +params['year'] : new Date(Date.now()).getFullYear();

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

  loadTable() {
    if (this.selectedValueType == null && this.selectedPeriodMode == this.PERIOD_MODE_YEARLY)
      return

    this.isGenerating = true
    this.forbiddenRequest = false
    this.errorOccurred = false

    this.updateFixedAndBoldTableColumns()

    // Year
    if (this.selectedPeriodMode == this.PERIOD_MODE_YEARLY) {
      this.loadYearlyData()
        .subscribe(this.getDataObserver(this.getTableHeaderStrings()))
    }
    // Month
    else {
      this.loadMonthlyData()
        .subscribe(this.getDataObserverWithoutSeparateHeader())
    }
  }

  getLocalDateString(dateString: string | null): string {
    return this.converter.getLocalDateString(dateString);
  }

  getMonthName(month: number): string {
    const date = new Date();
    date.setMonth(month - 1);
    return date.toLocaleString('de-DE', { month: 'long' });
  }

  openPermissionInfoModal() {
    this.dialog.open(OverviewPermissionInfoModalComponent)
  }

  openValueTypeInfoModal() {
    this.dialog.open(OverviewValueTypeInfoModalComponent)
  }

  private updateFixedAndBoldTableColumns() {
    if (this.selectedPeriodMode == this.PERIOD_MODE_YEARLY) {
      this.columnFixedWidthFromIndex = 0
      this.boldColumnIndices = [3]
    } else if (this.selectedPeriodMode == this.PERIOD_MODE_MONTHLY) {
      this.columnFixedWidthFromIndex = 0
      this.boldColumnIndices = [0]
    }

    this.columnFixedWidthFromIndex$.next(this.columnFixedWidthFromIndex)
    this.boldColumnIndices$.next(this.boldColumnIndices)
  }

  private loadYearlyData(): Observable<OverviewAssistancePlan[]> {
    return this.overviewService.getOverviewFromAssistancePlanByYear(
      this.year,
      this.selectedHourType?.id ?? null,
      this.selectedArea?.id ?? null,
      this.selectedSponsor?.id ?? null,
      this.selectedValueType!!)
  }

  private loadMonthlyData(): Observable<AssistancePlansAnalysisMonthDto> {
    return this.assistancePlanAnalysisService.getByYearAndMonthAndInstitutionIdAndSponsorIdAndHourTypeId(
      this.year,
      this.month,
      this.selectedArea?.id ?? 0,
      this.selectedSponsor?.id ?? 0,
      this.selectedHourType?.id ?? 0)
  }

  private generateTableData(source: OverviewAssistancePlan[]) {
    const data = source.map(value => {
      // client name
      let result = [(value.clientDto?.lastName ?? "") + " " + (value.clientDto?.firstName ?? "unbekannt")];

      // assistance plan end
      result.push(this.getLocalDateString(value.assistancePlanDto?.start ?? null));
      result.push(this.getLocalDateString(value.assistancePlanDto?.end ?? null));

      for (let i = 0; i < value.values.length; i++) {
        result.push(value.values[i].toString());
      }
      return result;
    });

    this.setTableData(data)
  }

  private setTableData(data: any[][]) {
    this.data = data
    this.data$.next(this.data)
  }

  private getTableHeaderStrings(): string[] {
    let daysArray: string[] = [this.CLIENT_COLUMN_HEADER, this.ASSISTANCE_PLAN_START_COLUMN_HEADER, this.ASSISTANCE_PLAN_END_COLUMN_HEADER];

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

  private validateGenerationStatus() {
    let yearlyGenerationAllowed = this.selectedPeriodMode == this.PERIOD_MODE_YEARLY &&
      this.selectedValueType != null &&
      this.selectedSponsor != null &&
      this.selectedArea != null &&
      this.selectedHourType != null

    let monthlyGenerationAllowed = this.selectedPeriodMode == this.PERIOD_MODE_MONTHLY &&
      this.selectedSponsor != null &&
      this.selectedArea != null &&
      this.selectedHourType != null

    this.generationAllowed = yearlyGenerationAllowed || monthlyGenerationAllowed
  }

  private getDataObserverWithoutSeparateHeader() {
    return {
      next: (value: AssistancePlansAnalysisMonthDto) => {
        let fullData = this.assistancePlanAnalysisService.convertToArray(value)
        let header = fullData[0]
        let data = fullData.slice(1)
        this.columns = header;
        this.columns$.next(this.columns);
        this.setTableData(data != undefined ? data : []);
        this.isGenerating = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isGenerating = false
        if (err.status == HttpStatusCode.Forbidden) {
          this.forbiddenRequest = true
        } else {
          this.errorOccurred = true
        }
      }
    };
  }

  private getDataObserver(header: string[]) {
    return {
      next: (value) => {
        this.columns = header;
        this.columns$.next(this.columns);
        this.generateTableData(value);
        this.isGenerating = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isGenerating = false
        if (err.status == HttpStatusCode.Forbidden) {
          this.forbiddenRequest = true
        } else {
          this.errorOccurred = true
        }
      }
    };
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
