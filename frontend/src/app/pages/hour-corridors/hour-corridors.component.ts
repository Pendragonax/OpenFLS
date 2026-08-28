import {Component, OnInit} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Sort} from '@angular/material/sort';
import {AbstractControl, UntypedFormControl, UntypedFormGroup, ValidationErrors, Validators} from '@angular/forms';
import {TablePageComponent} from '../../shared/components/table-page.component';
import {HelperService} from '../../shared/services/helper.service';
import {Comparer} from '../../shared/services/comparer.helper';
import {HourTypeDto} from '../../shared/dtos/hour-type-dto.model';
import {HourTypeService} from '../../shared/services/hour-type.service';
import {HourCorridorDto} from '../../shared/dtos/hour-corridor-dto.model';
import {HourCorridorService} from '../../shared/services/hour-corridor.service';
import {HourCorridorAuditHistoryComponent} from './hour-corridor-audit-history.component';

const weeklyDurationValidator = (control: AbstractControl): ValidationErrors | null => {
  const hours = Number(control.get('weeklyHoursPart')?.value ?? 0);
  const minutes = Number(control.get('weeklyMinutesPart')?.value ?? 0);

  if (!Number.isFinite(hours) || !Number.isFinite(minutes)) {
    return {weeklyDurationInvalid: true};
  }

  if (hours < 0 || hours > 9999 || minutes < 0 || minutes > 59) {
    return {weeklyDurationInvalid: true};
  }

  return null;
};

const corridorRangeValidator = (control: AbstractControl): ValidationErrors | null => {
  const fromHours = Number(control.get('weeklyHoursFromPart')?.value ?? 0);
  const fromMinutes = Number(control.get('weeklyMinutesFromPart')?.value ?? 0);
  const tillHours = Number(control.get('weeklyHoursTillPart')?.value ?? 0);
  const tillMinutes = Number(control.get('weeklyMinutesTillPart')?.value ?? 0);

  if (![fromHours, fromMinutes, tillHours, tillMinutes].every(Number.isFinite)) {
    return {corridorDurationInvalid: true};
  }

  const from = fromHours * 60 + fromMinutes;
  const till = tillHours * 60 + tillMinutes;

  if (from < 0 || till < 0 || fromMinutes > 59 || tillMinutes > 59 || fromHours > 9999 || tillHours > 9999) {
    return {corridorDurationInvalid: true};
  }

  if (till < from) {
    return {corridorRangeInvalid: true};
  }

  return null;
};

@Component({
  selector: 'app-hour-corridors',
  templateUrl: './hour-corridors.component.html',
  styleUrls: ['./hour-corridors.component.css'],
  standalone: false
})
export class HourCorridorsComponent extends TablePageComponent<HourCorridorDto, HourCorridorDto> implements OnInit {
  tableColumns = ['title', 'hourType', 'weeklyMinutesFrom', 'weeklyMinutesTill', 'assistancePlanCount', 'actions'];

  hourTypes: HourTypeDto[] = [];
  deleteValue: HourCorridorDto | null = null;

  editForm = new UntypedFormGroup({
    title: new UntypedFormControl('', Validators.compose([Validators.required, Validators.minLength(1), Validators.maxLength(64)])),
    hourType: new UntypedFormControl(null, Validators.compose([Validators.required, Validators.min(1)])),
    weeklyHoursFromPart: new UntypedFormControl(0, Validators.compose([Validators.min(0), Validators.max(9999), Validators.required])),
    weeklyMinutesFromPart: new UntypedFormControl(0, Validators.compose([Validators.min(0), Validators.max(59), Validators.required])),
    weeklyHoursTillPart: new UntypedFormControl(0, Validators.compose([Validators.min(0), Validators.max(9999), Validators.required])),
    weeklyMinutesTillPart: new UntypedFormControl(0, Validators.compose([Validators.min(0), Validators.max(59), Validators.required]))
  }, {validators: [weeklyDurationValidator, corridorRangeValidator]});

  get titleControl() { return this.editForm.controls['title']; }

  get hourTypeControl() { return this.editForm.controls['hourType']; }

  get weeklyHoursFromPartControl() { return this.editForm.controls['weeklyHoursFromPart']; }

  get weeklyMinutesFromPartControl() { return this.editForm.controls['weeklyMinutesFromPart']; }

  get weeklyHoursTillPartControl() { return this.editForm.controls['weeklyHoursTillPart']; }

  get weeklyMinutesTillPartControl() { return this.editForm.controls['weeklyMinutesTillPart']; }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private hourCorridorService: HourCorridorService,
    private hourTypeService: HourTypeService,
    private comparer: Comparer
  ) {
    super(modalService, helperService);
  }

  override loadReferenceValues() {
    this.hourTypeService.allValues$.subscribe(values => {
      this.hourTypes = values;
      this.refreshTablePage();
    });
  }

  loadValues() {
    if (this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;

    this.hourCorridorService.allValues$.subscribe({
      next: values => {
        this.values = values;
        this.values$.next(values);
        this.filteredTableData = values;
        this.refreshTablePage();
        this.isSubmitting = false;
      },
      error: () => this.handleFailure('Fehler beim laden')
    });
  }

  override handleDeleteModalOpen(value: HourCorridorDto) {
    this.deleteValue = value;
  }

  override handleDeleteModalClosed() {
    this.deleteValue = null;
  }

  getNewValue(): HourCorridorDto {
    return new HourCorridorDto();
  }

  initFormSubscriptions() {
    this.titleControl.valueChanges.subscribe(value => this.editValue.title = value);
    this.hourTypeControl.valueChanges.subscribe(value => this.editValue.hourTypeId = Number(value ?? 0));
    this.weeklyHoursFromPartControl.valueChanges.subscribe(() => this.syncWeeklyMinutes());
    this.weeklyMinutesFromPartControl.valueChanges.subscribe(() => this.syncWeeklyMinutes());
    this.weeklyHoursTillPartControl.valueChanges.subscribe(() => this.syncWeeklyMinutes());
    this.weeklyMinutesTillPartControl.valueChanges.subscribe(() => this.syncWeeklyMinutes());
  }

  fillEditForm(value: HourCorridorDto) {
    const fromMinutes = Math.max(0, Math.round(Number(value.weeklyMinutesFrom ?? 0)));
    const tillMinutes = Math.max(0, Math.round(Number(value.weeklyMinutesTill ?? 0)));

    this.editForm.setValue({
      title: value.title,
      hourType: value.hourTypeId > 0 ? value.hourTypeId : null,
      weeklyHoursFromPart: Math.floor(fromMinutes / 60),
      weeklyMinutesFromPart: fromMinutes % 60,
      weeklyHoursTillPart: Math.floor(tillMinutes / 60),
      weeklyMinutesTillPart: tillMinutes % 60
    }, {emitEvent: false});

    this.editValue = {
      ...this.editValue,
      title: value.title,
      hourTypeId: value.hourTypeId > 0 ? value.hourTypeId : 0,
      weeklyMinutesFrom: fromMinutes,
      weeklyMinutesTill: tillMinutes
    };

    this.syncWeeklyMinutes();
  }

  create(value: HourCorridorDto) {
    if (this.isSubmitting) {
      return;
    }

    if (!this.editForm.valid) {
      return;
    }

    this.isSubmitting = true;

    this.hourCorridorService.create(value).subscribe({
      next: () => this.handleSuccess('Stundenkorridor erfolgreich gespeichert'),
      error: () => this.handleFailure('Fehler beim speichern')
    });
  }

  update(value: HourCorridorDto) {
    if (this.isSubmitting) {
      return;
    }

    if (!this.editForm.valid || value.id <= 0) {
      return;
    }

    this.isSubmitting = true;

    this.hourCorridorService.update(value.id, value).subscribe({
      next: () => this.handleSuccess('Stundenkorridor erfolgreich geändert'),
      error: () => this.handleFailure('Fehler beim speichern')
    });
  }

  delete(value: HourCorridorDto) {
    if (this.isSubmitting || this.isDeleteDisabled(value)) {
      return;
    }

    this.isSubmitting = true;

    this.hourCorridorService.delete(value.id).subscribe({
      next: () => this.handleSuccess('Stundenkorridor gelöscht'),
      error: () => this.handleFailure('Fehler beim löschen')
    });
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(value => {
      const hourTypeTitle = this.resolveHourTypeTitle(value.hourTypeId, value.hourTypeTitle).toLowerCase();
      return value.title.toLowerCase().includes(this.searchString)
        || hourTypeTitle.includes(this.searchString);
    });

    this.refreshTablePage();
  }

  sortData(sort: Sort) {
    const data = this.tableSource.data.slice();
    if (!sort.active || sort.direction === '') {
      this.tableSource.data = data;
      return;
    }

    this.tableSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case this.tableColumns[0]:
          return this.comparer.compare(a.title, b.title, isAsc);
        case this.tableColumns[1]:
          return this.comparer.compare(this.resolveHourTypeTitle(a.hourTypeId, a.hourTypeTitle), this.resolveHourTypeTitle(b.hourTypeId, b.hourTypeTitle), isAsc);
        case this.tableColumns[2]:
          return this.comparer.compare(a.weeklyMinutesFrom, b.weeklyMinutesFrom, isAsc);
        case this.tableColumns[3]:
          return this.comparer.compare(a.weeklyMinutesTill, b.weeklyMinutesTill, isAsc);
        case this.tableColumns[4]:
          return this.comparer.compare(a.assistancePlanCount, b.assistancePlanCount, isAsc);
        default:
          return 0;
      }
    });
  }

  fillTableSource(values: HourCorridorDto[]) {
    this.filteredTableData = values;
    this.refreshTablePage();
  }

  getHourTypeTitle(value: HourCorridorDto) {
    return this.resolveHourTypeTitle(value.hourTypeId, value.hourTypeTitle);
  }

  formatWeeklyMinutes(value: number): number {
    const totalMinutes = Math.max(0, Math.round(Number(value ?? 0)));
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return Number((hours + minutes / 100).toFixed(2));
  }

  isDeleteDisabled(value: HourCorridorDto) {
    return (value.assistancePlanCount ?? 0) > 0;
  }

  onSearchStringChanges(searchString: string) {
    this.searchString = (searchString ?? '').toLowerCase();
    this.filterTableData();
  }

  getDeleteHint(value: HourCorridorDto) {
    if (!this.isDeleteDisabled(value)) {
      return '';
    }

    return `${value.assistancePlanCount} Hilfepläne verknüpft`;
  }

  openHistoryModal(corridor: HourCorridorDto) {
    const modalRef = this.modalService.open(HourCorridorAuditHistoryComponent, {centered: true, size: 'lg', scrollable: true});
    modalRef.componentInstance.corridorId = corridor.id;
    modalRef.componentInstance.corridorTitle = corridor.title;
  }

  selectAll(event: FocusEvent) {
    const target = event.target as HTMLInputElement | null;
    if (!target) {
      return;
    }
    target.select();
  }

  clampWeeklyDuration() {
    this.weeklyHoursFromPartControl.setValue(this.toBoundedInt(this.weeklyHoursFromPartControl.value, 0, 9999));
    this.weeklyMinutesFromPartControl.setValue(this.toBoundedInt(this.weeklyMinutesFromPartControl.value, 0, 59));
    this.weeklyHoursTillPartControl.setValue(this.toBoundedInt(this.weeklyHoursTillPartControl.value, 0, 9999));
    this.weeklyMinutesTillPartControl.setValue(this.toBoundedInt(this.weeklyMinutesTillPartControl.value, 0, 59));
    this.syncWeeklyMinutes();
  }

  private syncWeeklyMinutes() {
    const fromHours = this.toBoundedInt(this.weeklyHoursFromPartControl.value, 0, 9999);
    const fromMinutes = this.toBoundedInt(this.weeklyMinutesFromPartControl.value, 0, 59);
    const tillHours = this.toBoundedInt(this.weeklyHoursTillPartControl.value, 0, 9999);
    const tillMinutes = this.toBoundedInt(this.weeklyMinutesTillPartControl.value, 0, 59);

    this.editValue.weeklyMinutesFrom = fromHours * 60 + fromMinutes;
    this.editValue.weeklyMinutesTill = tillHours * 60 + tillMinutes;
  }

  private resolveHourTypeTitle(hourTypeId: number, fallbackTitle: string) {
    return this.hourTypes.find(type => type.id === hourTypeId)?.title ?? fallbackTitle ?? 'n/a';
  }

  private toBoundedInt(value: unknown, min: number, max: number) {
    const parsed = Number(value ?? 0);
    if (!Number.isFinite(parsed)) {
      return min;
    }

    return Math.max(min, Math.min(max, Math.floor(parsed)));
  }
}
