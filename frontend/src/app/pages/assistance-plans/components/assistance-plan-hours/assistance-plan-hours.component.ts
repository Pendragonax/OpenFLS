import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Sort} from '@angular/material/sort';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {AbstractControl, UntypedFormControl, UntypedFormGroup, ValidationErrors, Validators} from '@angular/forms';
import {TablePageComponent} from '../../../../shared/components/table-page.component';
import {AssistancePlanHourDto} from '../../../../shared/dtos/assistance-plan-hour-dto.model';
import {HourTypeDto} from '../../../../shared/dtos/hour-type-dto.model';
import {HelperService} from '../../../../shared/services/helper.service';
import {HourTypeService} from '../../../../shared/services/hour-type.service';
import {AssistancePlanCreateHourDto} from '../../../../shared/dtos/assistance-plan-create-dto.model';

const weeklyDurationValidator = (control: AbstractControl): ValidationErrors | null => {
  const hours = Number(control.get('weeklyHoursPart')?.value ?? 0);
  const minutes = Number(control.get('weeklyMinutesPart')?.value ?? 0);

  if (!Number.isFinite(hours) || !Number.isFinite(minutes)) {
    return {weeklyDurationInvalid: true};
  }

  if (hours < 0 || minutes < 0 || minutes > 59) {
    return {weeklyDurationInvalid: true};
  }

  return (hours * 60 + minutes) > 0 ? null : {weeklyDurationRequired: true};
};

@Component({
  selector: 'app-assistance-plan-hours-page',
  templateUrl: './assistance-plan-hours.component.html',
  styleUrls: ['./assistance-plan-hours.component.css'],
  standalone: false
})
export class AssistancePlanHoursPageComponent
  extends TablePageComponent<AssistancePlanHourDto, [AssistancePlanHourDto, HourTypeDto | null]>
  implements OnInit {

  @Input() editable = false;
  @Input() set hours(value: AssistancePlanCreateHourDto[]) {
    this.values = (value ?? []).map((hour, index) => ({
      id: index + 1,
      weeklyHours: hour.weeklyHours,
      hourTypeId: hour.hourTypeId,
      assistancePlanId: 0
    }));
    this.filteredTableData = this.convertToTableSource(this.values);
    this.refreshTablePage();
  }

  @Output() hoursChange = new EventEmitter<AssistancePlanCreateHourDto[]>();

  tableColumns = ['type', 'weeklyHours', 'action'];

  hourTypes: HourTypeDto[] = [];
  filteredHourTypes: HourTypeDto[] = [];

  editForm = new UntypedFormGroup({
    type: new UntypedFormControl(null, Validators.compose([Validators.required])),
    weeklyHoursPart: new UntypedFormControl(0, Validators.compose([
      Validators.min(0),
      Validators.max(9999),
      Validators.required
    ])),
    weeklyMinutesPart: new UntypedFormControl(0, Validators.compose([
      Validators.min(0),
      Validators.max(59),
      Validators.required
    ]))
  }, {validators: weeklyDurationValidator});

  get typeControl() { return this.editForm.controls['type']; }

  get weeklyHoursPartControl() { return this.editForm.controls['weeklyHoursPart']; }

  get weeklyMinutesPartControl() { return this.editForm.controls['weeklyMinutesPart']; }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private hourTypeService: HourTypeService
  ) {
    super(modalService, helperService);
  }

  loadValues() {
    this.hourTypeService.allValues$.subscribe(types => {
      this.hourTypes = types;
      this.filteredTableData = this.convertToTableSource(this.values);
      this.refreshTablePage();
    });
  }

  getNewValue(): AssistancePlanHourDto {
    return new AssistancePlanHourDto();
  }

  convertToTableSource(values: AssistancePlanHourDto[]): [AssistancePlanHourDto, HourTypeDto | null][] {
    return values.map(value => [
      value,
      this.hourTypes.find(type => type.id === value.hourTypeId) ?? null
    ]);
  }

  initFormSubscriptions() {
    this.weeklyHoursPartControl.valueChanges.subscribe(() => this.syncWeeklyHoursFromParts());
    this.weeklyMinutesPartControl.valueChanges.subscribe(() => this.syncWeeklyHoursFromParts());
    this.typeControl.valueChanges.subscribe(value => {
      this.editValue.hourTypeId = this.hourTypes.find(type => type.id === value)?.id ?? 0;
    });
  }

  fillEditForm(value: AssistancePlanHourDto) {
    const totalMinutes = Math.max(0, Math.round(Number(value.weeklyHours ?? 0) * 60));
    const hoursPart = Math.floor(totalMinutes / 60);
    const minutesPart = totalMinutes % 60;
    this.weeklyHoursPartControl.setValue(hoursPart);
    this.weeklyMinutesPartControl.setValue(minutesPart);
    this.typeControl.setValue(value.hourTypeId);
    this.syncWeeklyHoursFromParts();
  }

  filterTableData() {
    this.refreshTablePage();
  }

  override filterReferenceValues() {
    this.filteredHourTypes = this.hourTypes.filter(type => {
      if (this.modalEditMode && this.editValue.hourTypeId === type.id) {
        return true;
      }
      return !this.values.some(value => value.hourTypeId === type.id);
    });
  }

  create(value: AssistancePlanHourDto) {
    const withId = {
      ...value,
      id: this.values.length > 0 ? Math.max(...this.values.map(v => v.id)) + 1 : 1
    };
    this.values = [...this.values, withId];
    this.emitHours();
  }

  update(value: AssistancePlanHourDto) {
    this.values = this.values.map(current => current.id === value.id ? {...value} : current);
    this.emitHours();
  }

  delete(value: AssistancePlanHourDto) {
    this.values = this.values.filter(current => current.id !== value.id);
    this.emitHours();
  }

  resetHourType() {
    this.typeControl.setValue(null);
  }

  getHourTypeName(value: HourTypeDto | null) {
    if (value == null) {
      return 'n/a';
    }

    return value.title;
  }

  sortData(sort: Sort) {
  }

  clampWeeklyDuration() {
    const safeHours = this.toBoundedInt(this.weeklyHoursPartControl.value, 0, 9999);
    const safeMinutes = this.toBoundedInt(this.weeklyMinutesPartControl.value, 0, 59);
    this.weeklyHoursPartControl.setValue(safeHours);
    this.weeklyMinutesPartControl.setValue(safeMinutes);
    this.syncWeeklyHoursFromParts();
  }

  selectAll(event: FocusEvent) {
    const target = event.target as HTMLInputElement | null;
    if (!target) {
      return;
    }
    target.select();
  }

  private emitHours() {
    this.filteredTableData = this.convertToTableSource(this.values);
    this.refreshTablePage();
    this.hoursChange.emit(this.values.map(value => ({
      weeklyHours: value.weeklyHours,
      hourTypeId: value.hourTypeId
    })));
  }

  private syncWeeklyHoursFromParts() {
    const hoursPart = this.toBoundedInt(this.weeklyHoursPartControl.value, 0, 9999);
    const minutesPart = this.toBoundedInt(this.weeklyMinutesPartControl.value, 0, 59);
    const totalMinutes = hoursPart * 60 + minutesPart;
    this.editValue.weeklyHours = Number((totalMinutes / 60).toFixed(4));
  }

  private toBoundedInt(value: unknown, min: number, max: number) {
    const parsed = Number(value ?? 0);
    if (!Number.isFinite(parsed)) {
      return min;
    }
    return Math.max(min, Math.min(max, Math.floor(parsed)));
  }
}
