import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {Subscription} from 'rxjs';
import {LogEntryDto, LogSettingsDto} from '../../shared/dtos/log-entry-dto.model';
import {LogAdministrationService} from '../../shared/services/log-administration.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {TemplateRef} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationModalComponent} from '../../shared/modals/confirmation-modal/confirmation-modal.component';
import {DateCompleteSelectionComponent} from '../../shared/components/date-complete-selection/date-complete-selection.component';

@Component({selector: 'app-settings', templateUrl: './settings.component.html', styleUrls: ['./settings.component.css'], standalone: false})
export class SettingsComponent implements OnInit, OnDestroy {
  readonly levels = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'];
  entries: LogEntryDto[] = [];
  page = 0;
  pageSize = 100;
  totalEntries = 0;
  hasNextPage = false;
  days: string[] = [];
  settings: LogSettingsDto | null = null;
  mobileTab: 'filter' | 'live' | 'levels' = 'filter';
  private liveSubscription?: Subscription;
  private readonly newEntryKeys = new Set<string>();
  @ViewChild(DateCompleteSelectionComponent) dateSelection?: DateCompleteSelectionComponent;
  filter = new UntypedFormGroup({from: new UntypedFormControl(new Date()), to: new UntypedFormControl(new Date()), query: new UntypedFormControl(''), level: new UntypedFormControl(''), logger: new UntypedFormControl(''), thread: new UntypedFormControl(''), all: new UntypedFormControl(false)});

  classLevelForm = new UntypedFormGroup({logger: new UntypedFormControl(''), level: new UntypedFormControl('INFO')});
  constructor(private logs: LogAdministrationService, private modal: NgbModal, private dialog: MatDialog) {}
  ngOnInit(): void { this.load(); this.liveSubscription = this.logs.liveEntries().subscribe(entry => this.addLiveEntry(entry)); }
  ngOnDestroy(): void { this.liveSubscription?.unsubscribe(); }
  load(): void {
    this.page = 0;
    this.logs.days().subscribe(days => this.days = days);
    this.loadPage();
    this.logs.settings().subscribe(settings => this.settings = settings);
  }
  loadPage(): void {
    this.logs.page(this.query(), this.page, this.pageSize).subscribe(result => {
      this.entries = result.content;
      this.totalEntries = result.totalElements;
      this.hasNextPage = result.hasNext;
    });
  }
  previousPage(): void { if (this.page > 0) { this.page--; this.loadPage(); } }
  nextPage(): void { if (this.hasNextPage) { this.page++; this.loadPage(); } }
  totalPages(): number { return Math.max(1, Math.ceil(this.totalEntries / this.pageSize)); }
  resetFilters(): void { const today = new Date(); this.filter.reset({from: today, to: today, query: '', level: '', logger: '', thread: '', all: false}); this.dateSelection?.setRange(today, today); this.load(); }
  onDateChanged(value: {start: Date; end: Date}): void { this.filter.patchValue({from: value.start, to: value.end}); }
  setGlobalLevel(level: string): void { this.logs.setLevel('ROOT', level).subscribe(settings => this.settings = settings); }
  setClassLevel(logger: string, level: string): void { this.logs.setLevel(logger, level || null).subscribe(settings => this.settings = settings); }
  openClassLevelModal(content: TemplateRef<unknown>, configured?: {logger: string; level: string | null}): void { this.classLevelForm.reset({logger: configured?.logger || '', level: configured?.level || 'INFO'}); this.modal.open(content, {centered: true, size: 'md'}); }
  addClassLevel(modal: {close: () => void}): void { const logger = String(this.classLevelForm.value.logger || '').trim(); const level = String(this.classLevelForm.value.level || ''); if (!logger || !level) return; this.logs.setLevel(logger, level).subscribe(settings => { this.settings = settings; modal.close(); }); }
  resetLevels(): void { this.openConfirmation('Alle konfigurierten Log-Level auf die beim Anwendungsstart gültigen Werte zurücksetzen?', () => this.logs.resetLevels().subscribe(settings => this.settings = settings)); }
  resetClassLevel(logger: string): void { this.openConfirmation(`Die Ausnahme für „${logger}“ auf den Startwert zurücksetzen?`, () => this.logs.resetLevel(logger).subscribe(settings => this.settings = settings)); }
  deleteAll(): void { this.openConfirmation('Alle archivierten Logdateien unwiderruflich löschen?', () => this.logs.deleteFrom().subscribe(() => this.load())); }
  deleteFrom(entry: LogEntryDto): void { this.openConfirmation(`Den ausgewählten Eintrag und alle älteren Logeinträge bis ${new Date(entry.timestamp).toLocaleString()} löschen?`, () => this.logs.deleteFrom(entry.timestamp).subscribe(() => this.load())); }
  export(): void { this.logs.export(this.query()).subscribe(data => { const link = document.createElement('a'); link.href = URL.createObjectURL(data); link.download = 'openfls-logs.zip'; link.click(); URL.revokeObjectURL(link.href); }); }
  levelClass(level: string): string { return `log-${level.toLowerCase()}`; }
  isNew(entry: LogEntryDto): boolean { return this.newEntryKeys.has(this.entryKey(entry)); }
  private addLiveEntry(entry: LogEntryDto): void {
    if (this.page !== 0) return;
    if (!this.matches(entry)) return;
    const key = this.entryKey(entry);
    this.newEntryKeys.add(key);
    this.entries = [entry, ...this.entries].slice(0, 5000);
    this.totalEntries++;
    window.setTimeout(() => this.newEntryKeys.delete(key), 1250);
  }
  private matches(entry: LogEntryDto): boolean {
    const filter = this.filter.value;
    const date = entry.timestamp.substring(0, 10);
    const from = this.dateValue(filter.from); const to = this.dateValue(filter.to);
    return (filter.all || (!from || date >= from) && (!to || date <= to)) && (!filter.level || entry.level === filter.level) && (!filter.logger || entry.logger.toLowerCase().includes(filter.logger.toLowerCase())) && (!filter.thread || entry.thread.toLowerCase().includes(filter.thread.toLowerCase())) && (!filter.query || `${entry.message} ${entry.logger}`.toLowerCase().includes(filter.query.toLowerCase()));
  }
  private query() { return {...this.filter.value, from: this.dateValue(this.filter.value.from), to: this.dateValue(this.filter.value.to)}; }
  private dateValue(value: Date | string | null | undefined): string | undefined { if (!value) return undefined; const date = value instanceof Date ? value : new Date(value); return Number.isNaN(date.valueOf()) ? undefined : `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`; }
  private entryKey(entry: LogEntryDto): string { return `${entry.timestamp}|${entry.level}|${entry.logger}|${entry.thread}|${entry.message}`; }
  private openConfirmation(description: string, operation: () => void): void {
    const dialogRef = this.dialog.open(ConfirmationModalComponent);
    dialogRef.componentInstance.title = 'Bestätigung';
    dialogRef.componentInstance.description = description;
    dialogRef.afterClosed().subscribe(confirmed => { if (confirmed) operation(); });
  }
}
