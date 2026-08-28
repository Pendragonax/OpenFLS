import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {combineLatest, ReplaySubject} from 'rxjs';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {Sort} from '@angular/material/sort';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationModalComponent} from '../../../shared/modals/confirmation-modal/confirmation-modal.component';
import {TablePageComponent} from '../../../shared/components/table-page.component';
import {HelperService} from '../../../shared/services/helper.service';
import {AssistancePlanService} from '../../../shared/services/assistance-plan.service';
import {InstitutionService} from '../../../shared/services/institution.service';
import {ServiceService} from '../../../shared/services/service.service';
import {EmployeeService} from '../../../shared/services/employee.service';
import {UserService} from '../../../shared/services/user.service';
import {Comparer} from '../../../shared/services/comparer.helper';
import {Converter} from '../../../shared/services/converter.helper';
import {InstitutionDto} from '../../../shared/dtos/institution-dto.model';
import {SponsorDto} from '../../../shared/dtos/sponsor-dto.model';
import {ClientViewModel} from '../../../shared/models/client-view.model';
import {InstitutionViewModel} from '../../../shared/models/institution-view.model';
import {AssistancePlanPreviewDto} from '../../../shared/dtos/assistance-plan-preview-dto.model';
import {AssistancePlanHourMode} from '../../../shared/dtos/assistance-plan-hour-mode.model';

type AssistancePlanPreviewRow = {
  preview: AssistancePlanPreviewDto;
  editable: boolean;
};

type AssistancePlanContext = 'none' | 'client' | 'sponsor' | 'institution' | 'favorites';
type AssistancePlanStatusPeriod = 'assistancePlan' | 'year';

@Component({
  selector: 'app-assistance-plans',
  templateUrl: './assistance-plans.component.html',
  styleUrls: ['./assistance-plans.component.css'],
  providers: [
    {provide: MAT_DATE_LOCALE, useValue: 'de-DE'},
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE]
    },
    {provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS}
  ],
  standalone: false
})
export class AssistancePlansComponent
  extends TablePageComponent<AssistancePlanPreviewDto, AssistancePlanPreviewRow>
  implements OnInit, OnChanges {
  readonly illegalHoursTooltip = 'Sowohl der Hilfeplan als auch die Ziele haben Stunden zugewiesen. Dies stellt ein Problem bei der Berechnung dar.';
  readonly AssistancePlanHourMode = AssistancePlanHourMode;

  @Input() client$: ReplaySubject<ClientViewModel> = new ReplaySubject<ClientViewModel>();
  @Input() sponsor$: ReplaySubject<SponsorDto> = new ReplaySubject<SponsorDto>();
  @Input() institution$: ReplaySubject<InstitutionViewModel> = new ReplaySubject<InstitutionViewModel>();
  @Input() favorites$: ReplaySubject<boolean> = new ReplaySubject<boolean>();
  @Input() hideInstitutionColumn: boolean = false;
  @Input() hideClientColumn: boolean = false;
  @Input() hideSponsorColumn: boolean = false;
  @Input() hideAddButton: boolean = false;
  @Input() readOnly: boolean = false;
  @Input() hideInstitutionFilter: boolean = false;
  @Input() hideSearchStringFilter: boolean = false;
  @Input() showArchivedEntries: boolean = false;
  @Input() showArchivedEntriesToggleVisible: boolean = false;
  @Output() addedValueEvent = new EventEmitter<AssistancePlanPreviewDto>();
  @Output() updatedValueEvent = new EventEmitter<AssistancePlanPreviewDto>();
  @Output() deletedValueEvent = new EventEmitter<AssistancePlanPreviewDto>();

  tableColumns = ['client', 'institution', 'sponsor', 'start', 'end', 'status', 'hours', 'yearStatus', 'actions'];
  statusPeriod: AssistancePlanStatusPeriod = 'assistancePlan';

  deleteServiceCount: number = 0;
  client: ClientViewModel = new ClientViewModel();
  sponsor: SponsorDto = new SponsorDto();
  institution: InstitutionViewModel = new InstitutionViewModel();
  tableData: AssistancePlanPreviewRow[] = [];
  institutions: InstitutionDto[] = [];
  addAvailable: boolean = false;

  filterDate: Date | null = null;
  filterInstitutionId: number | null = null;
  showArchivedEntriesVisible = false;

  override filterForm = new UntypedFormGroup({
    searchString: new UntypedFormControl(''),
    date: new UntypedFormControl(),
    institution: new UntypedFormControl()
  });

  private currentContext: AssistancePlanContext = 'none';
  private currentContextId: number = 0;

  get dateControl() {
    return this.filterForm.controls['date'];
  }

  get institutionControl() {
    return this.filterForm.controls['institution'];
  }

  isRowReadOnly(row: AssistancePlanPreviewRow): boolean {
    return this.readOnly || row.preview.clientArchived;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['showArchivedEntries']) {
      this.showArchivedEntriesVisible = changes['showArchivedEntries'].currentValue ?? false;
      if (!changes['showArchivedEntries'].firstChange) {
        this.filterTableData();
      }
    }
  }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private assistancePlanService: AssistancePlanService,
    private institutionService: InstitutionService,
    private serviceService: ServiceService,
    private employeeService: EmployeeService,
    private userService: UserService,
    private comparer: Comparer,
    private matDialog: MatDialog,
    private converter: Converter
  ) {
    super(modalService, helperService);
  }

  getNewValue(): AssistancePlanPreviewDto {
    return new AssistancePlanPreviewDto();
  }

  override loadReferenceValues() {
    this.institutionService.allValues$.subscribe({
      next: (institutions) => {
        this.institutions = institutions;
      }
    });
  }

  override loadValues() {
    this.client$.subscribe({
      next: (value) => {
        this.client = value;
        this.addAvailable = true;
        this.currentContext = 'client';
        this.currentContextId = value.dto.id;
        this.loadPreviewByClientId(value.dto.id);
      }
    });

    this.sponsor$.subscribe({
      next: (value) => {
        this.sponsor = value;
        this.addAvailable = false;
        this.currentContext = 'sponsor';
        this.currentContextId = value.id;
        this.loadPreviewBySponsorId(value.id);
      }
    });

    this.institution$.subscribe({
      next: (value) => {
        this.institution = value;
        this.addAvailable = false;
        this.currentContext = 'institution';
        this.currentContextId = value.dto.id;
        this.loadPreviewByInstitutionId(value.dto.id);
      }
    });

    this.favorites$.subscribe({
      next: () => {
        this.addAvailable = false;
        this.currentContext = 'favorites';
        this.currentContextId = 0;
        this.loadPreviewByFavorites();
      }
    });
  }

  private loadPreviewByClientId(clientId: number) {
    this.isSubmitting = true;

    combineLatest([
      this.assistancePlanService.getPreviewByClientId(clientId),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ]).subscribe({
      next: ([previews, affiliatedInstitutions, isAdmin]) => {
        this.isSubmitting = false;
        this.setTableSource(this.buildRows(previews, affiliatedInstitutions, isAdmin));
      },
      error: () => {
        this.isSubmitting = false;
        this.handleFailure('Fehler beim Laden der Hilfepläne');
      }
    });
  }

  private loadPreviewByInstitutionId(institutionId: number) {
    this.isSubmitting = true;

    combineLatest([
      this.assistancePlanService.getPreviewByInstitutionId(institutionId),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ]).subscribe({
      next: ([previews, affiliatedInstitutions, isAdmin]) => {
        this.isSubmitting = false;
        this.setTableSource(this.buildRows(previews, affiliatedInstitutions, isAdmin));
      },
      error: () => {
        this.isSubmitting = false;
        this.handleFailure('Fehler beim Laden der Hilfepläne');
      }
    });
  }

  private loadPreviewBySponsorId(sponsorId: number) {
    this.isSubmitting = true;

    combineLatest([
      this.assistancePlanService.getPreviewBySponsorId(sponsorId),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ]).subscribe({
      next: ([previews, affiliatedInstitutions, isAdmin]) => {
        this.isSubmitting = false;
        this.setTableSource(this.buildRows(previews, affiliatedInstitutions, isAdmin));
      },
      error: () => {
        this.isSubmitting = false;
        this.handleFailure('Fehler beim Laden der Hilfepläne');
      }
    });
  }

  private loadPreviewByFavorites() {
    this.isSubmitting = true;

    combineLatest([
      this.assistancePlanService.getPreviewByFavorites(),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$
    ]).subscribe({
      next: ([previews, affiliatedInstitutions, isAdmin]) => {
        this.isSubmitting = false;
        this.setTableSource(this.buildRows(previews, affiliatedInstitutions, isAdmin));
      },
      error: () => {
        this.isSubmitting = false;
        this.handleFailure('Fehler beim Laden der Hilfepläne');
      }
    });
  }

  private buildRows(
    previews: AssistancePlanPreviewDto[],
    affiliatedInstitutions: number[],
    isAdmin: boolean
  ): AssistancePlanPreviewRow[] {
    return previews.map((preview) => {
      return {
        preview,
        editable: isAdmin || affiliatedInstitutions.some((institutionId) => institutionId === preview.institutionId)
      };
    });
  }

  override initFormSubscriptions() {}

  override fillEditForm(value: AssistancePlanPreviewDto) {
    throw new Error('Method not implemented.');
  }

  override initFilterFormSubscriptions() {
    super.initFilterFormSubscriptions();

    this.dateControl.valueChanges.subscribe((value) => this.setFilterDate(value));
    this.institutionControl.valueChanges.subscribe((value) => this.setFilterInstitution(value));
  }

  setFilterDate(value: unknown) {
    if (value != null) {
      this.filterDate = new Date(value.toString());
      this.filterTableData();
      return;
    }

    this.filterDate = null;
  }

  resetFilterDate() {
    this.dateControl.setValue(null);
    this.filterDate = null;
    this.filterTableData();
  }

  setTableSource(rows: AssistancePlanPreviewRow[]) {
    this.tableData = rows;
    this.filterTableData();
  }

  setFilterInstitution(value: number | null) {
    if (value != null) {
      this.filterInstitutionId = value;
      this.filterTableData();
      return;
    }

    this.filterInstitutionId = null;
  }

  onArchivedVisibilityChanged(showArchivedEntries: boolean) {
    this.showArchivedEntriesVisible = showArchivedEntries;
    this.filterTableData();
  }

  resetFilterInstitution() {
    this.institutionControl.setValue(null);
    this.filterInstitutionId = null;
    this.filterTableData();
  }

  override create(value: AssistancePlanPreviewDto) {
    throw new Error('Method not implemented.');
  }

  override update(value: AssistancePlanPreviewDto) {
    throw new Error('Method not implemented.');
  }

  override delete(value: AssistancePlanPreviewDto) {
    this.assistancePlanService.delete(value.id).subscribe({
      next: () => this.handleSuccess('Hilfeplan gelöscht'),
      error: () => this.handleFailure('Fehler beim löschen')
    });
  }

  override filterTableData() {
    let filteredData = this.tableData.slice();

    if (!this.showArchivedEntriesVisible) {
      filteredData = filteredData.filter((row) => !row.preview.clientArchived);
    }

    filteredData = filteredData.filter((row) => {
      const preview = row.preview;
      return preview.clientFirstname.toLowerCase().includes(this.searchString) ||
        preview.clientLastname.toLowerCase().includes(this.searchString) ||
        preview.institutionName.toLowerCase().includes(this.searchString) ||
        preview.sponsorName.toLowerCase().includes(this.searchString);
    });

    if (this.filterDate != null) {
      const filterDay = this.toDateOnly(this.filterDate);
      filteredData = filteredData.filter((row) => {
        const startDay = this.toDateOnly(new Date(row.preview.start));
        const endDay = this.toDateOnly(new Date(row.preview.end));
        return filterDay >= startDay && filterDay <= endDay;
      });
    }

    if (this.filterInstitutionId != null) {
      filteredData = filteredData.filter((row) => row.preview.institutionId === this.filterInstitutionId);
    }

    this.filteredTableData = filteredData;
    this.refreshTablePage();
  }

  private toDateOnly(value: Date): number {
    const date = new Date(value);
    date.setHours(0, 0, 0, 0);
    return date.getTime();
  }

  override handleDeleteModalOpen(value: AssistancePlanPreviewDto) {
    this.serviceService.getCountByAssistancePlanId(value.id).subscribe({
      next: (count) => this.deleteServiceCount = count
    });
  }

  override sortData(sort: Sort) {
    const data = this.tableData.slice();
    if (!sort.active || sort.direction === '') {
      this.tableSource.data = data;
      return;
    }

    this.tableSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case this.tableColumns[0]:
          return this.comparer.compare(a.preview.clientLastname, b.preview.clientLastname, isAsc);
        case this.tableColumns[1]:
          return this.comparer.compare(a.preview.institutionName, b.preview.institutionName, isAsc);
        case this.tableColumns[2]:
          return this.comparer.compare(a.preview.sponsorName, b.preview.sponsorName, isAsc);
        case this.tableColumns[3]:
          return this.comparer.compare(a.preview.start, b.preview.start, isAsc);
        case this.tableColumns[4]:
          return this.comparer.compare(a.preview.end, b.preview.end, isAsc);
        case this.tableColumns[5]:
          return this.comparer.compare(a.preview.isActive ? 1 : 0, b.preview.isActive ? 1 : 0, isAsc);
        case this.tableColumns[6]:
          return this.comparer.compare(a.preview.approvedHoursPerWeek, b.preview.approvedHoursPerWeek, isAsc);
        case this.tableColumns[7]:
          return this.comparer.compare(this.getExecutedHoursPercent(a.preview), this.getExecutedHoursPercent(b.preview), isAsc);
        default:
          return 0;
      }
    });
  }

  addAssistancePlanAsFavorite(id: number) {
    this.employeeService.addAssistancePlanFavorite(id).subscribe({
      next: () => this.reloadCurrentContext()
    });
  }

  deleteAssistancePlanAsFavorite(id: number) {
    this.openFavoriteDeleteConfirmationModal(() => {
      this.employeeService.deleteAssistancePlanFavorite(id).subscribe({
        next: () => this.reloadCurrentContext()
      });
    });
  }

  private reloadCurrentContext() {
    switch (this.currentContext) {
      case 'client':
        this.loadPreviewByClientId(this.currentContextId);
        break;
      case 'institution':
        this.loadPreviewByInstitutionId(this.currentContextId);
        break;
      case 'sponsor':
        this.loadPreviewBySponsorId(this.currentContextId);
        break;
      case 'favorites':
        this.loadPreviewByFavorites();
        break;
      default:
        break;
    }
  }

  openFavoriteDeleteConfirmationModal(operation: () => void) {
    const dialogRef = this.matDialog.open(ConfirmationModalComponent);
    const dialog = dialogRef.componentInstance;
    dialog.description = 'Wollen sie diesen Hilfeplan wirklich aus den Favoriten löschen?';
    dialogRef.afterClosed().subscribe({
      next: (value) => {
        if (value) {
          operation();
        }
      }
    });
  }

  getDateString(date: string | null): string {
    return this.converter.getLocalDateString(date);
  }

  getStatusLabel(isActive: boolean): string {
    return isActive ? 'Aktiv' : 'Inaktiv';
  }

  getHourModeRange(preview: AssistancePlanPreviewDto): string {
    if (preview.hourMode !== AssistancePlanHourMode.CORRIDOR) {
      return '';
    }

    return `${this.formatHourValue(preview.approvedHoursFrom)} - ${this.formatHourValue(preview.approvedHoursTo)}`;
  }

  getWeeklyHoursDisplay(preview: AssistancePlanPreviewDto): string {
    if (this.isCorridorPlan(preview)) {
      return this.getHourModeRange(preview);
    }

    return `${this.formatHourValue(preview.approvedHoursPerWeek)}`;
  }

  getExecutedHoursPercent(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod = 'year'): number {
    if (this.isCorridorPlan(preview)) {
      return this.getCorridorExecutedHoursPercent(preview, period);
    }

    const approvedMinutes = this.convertTimeDoubleToMinutes(this.getApprovedHours(preview, period));
    if (approvedMinutes <= 0) {
      return 0;
    }

    const executedMinutes = this.convertTimeDoubleToMinutes(this.getExecutedHours(preview, period));
    const percent = (executedMinutes * 100) / approvedMinutes;
    return Math.max(0, Math.min(100, Number(percent.toFixed(1))));
  }

  isCorridorPlan(preview: AssistancePlanPreviewDto): boolean {
    return preview.hourMode === AssistancePlanHourMode.CORRIDOR;
  }

  private convertTimeDoubleToMinutes(value: number): number {
    const sign = value < 0 ? -1 : 1;
    const absoluteValue = Math.abs(value);
    const hours = Math.trunc(absoluteValue);
    const minutes = Math.round((absoluteValue - hours) * 100);
    return sign * (hours * 60 + minutes);
  }

  private formatHourValue(value: number): string {
    return value.toLocaleString('de-DE', {maximumFractionDigits: 2});
  }

  getApprovedHoursLeftDisplay(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod = 'year'): string {
    const value = this.getApprovedHoursLeft(preview, period);
    const sign = value < 0 ? '-' : '+';
    return `${sign}${this.formatHourValue(Math.abs(value))}`;
  }

  getHoursTooltip(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod = 'year'): string {
    const executedHours = this.getExecutedHours(preview, period);
    const periodLabel = period === 'assistancePlan' ? 'Dieser Hilfeplan bis heute' : 'Dieses Jahr bis heute';
    if (this.isCorridorPlan(preview)) {
      return `${periodLabel}\nBewilligt von: ${this.getApprovedHoursFrom(preview, period)}\nBewilligt bis: ${this.getApprovedHoursTill(preview, period)}\nGeleistet: ${executedHours}`;
    }

    return `${periodLabel}\nBewilligt: ${this.getApprovedHours(preview, period)}\nGeleistet: ${executedHours}`;
  }

  getExecutedHoursProgressClass(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod = 'year'): string {
    const percent = this.getExecutedHoursPercent(preview, period);
    if (this.isCorridorPlan(preview)) {
      if (percent >= 40 && percent <= 60) {
        return 'hours-progress-fill--ok';
      }
      return 'hours-progress-fill--bad';
    }

    if (percent >= 95) {
      return 'hours-progress-fill--ok';
    }
    if (percent >= 90) {
      return 'hours-progress-fill--warn';
    }
    return 'hours-progress-fill--bad';
  }

  private getCorridorExecutedHoursPercent(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    const approvedFrom = this.convertTimeDoubleToMinutes(this.getApprovedHoursFrom(preview, period));
    const approvedTill = this.convertTimeDoubleToMinutes(this.getApprovedHoursTill(preview, period));
    const executed = this.convertTimeDoubleToMinutes(this.getExecutedHours(preview, period));

    if (approvedFrom <= 0 || approvedTill <= 0) {
      return 0;
    }

    if (executed < approvedFrom) {
      return this.clampPercent((executed * 40) / approvedFrom);
    }

    if (executed > approvedTill) {
      return this.clampPercent(60 + (executed * 40) / (approvedFrom + approvedTill));
    }

    const corridorWidth = approvedTill - approvedFrom;
    if (corridorWidth <= 0) {
      return 40;
    }

    return this.clampPercent(40 + ((executed - approvedFrom) * 20) / corridorWidth);
  }

  private clampPercent(value: number): number {
    return Math.max(0, Math.min(100, Number(value.toFixed(1))));
  }

  private getApprovedHoursFrom(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursThisAssistancePlanFrom : preview.approvedHoursThisYearFrom;
  }

  private getApprovedHoursTill(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursThisAssistancePlanTill : preview.approvedHoursThisYearTill;
  }

  private getApprovedHours(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursThisAssistancePlan : preview.approvedHoursThisYear;
  }

  private getExecutedHours(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.executedHoursThisAssistancePlan : preview.executedHoursThisYear;
  }

  private getApprovedHoursLeft(preview: AssistancePlanPreviewDto, period: AssistancePlanStatusPeriod): number {
    return period === 'assistancePlan' ? preview.approvedHoursLeftThisAssistancePlan : preview.approvedHoursLeftThisYear;
  }

  onSearchStringChanges(searchString: string) {
    this.searchString = searchString.toLowerCase();
    this.filterTableData();
  }
}
