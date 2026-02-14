import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {AssistancePlanHourDto} from "../../../../dtos/assistance-plan-hour-dto.model";
import {HourTypeDto} from "../../../../dtos/hour-type-dto.model";
import {AbstractControl, UntypedFormControl, UntypedFormGroup, ValidationErrors, Validators} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HourTypeService} from "../../../../services/hour-type.service";
import {combineLatest} from "rxjs";
import {AssistancePlanView} from "../../../../models/assistance-plan-view.model";
import {TablePageComponent} from "../../../table-page.component";
import {HelperService} from "../../../../services/helper.service";
import {Sort} from "@angular/material/sort";

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
    selector: 'app-assistance-plan-hours',
    templateUrl: './assistance-plan-hours.component.html',
    styleUrls: ['./assistance-plan-hours.component.css'],
    standalone: false
})
export class AssistancePlanHoursComponent
  extends TablePageComponent<AssistancePlanHourDto, [AssistancePlanHourDto, HourTypeDto | null]>
  implements OnInit {

  @Input() assistancePlanView: AssistancePlanView = new AssistancePlanView();
  @Input() assistancePlanView$: ReplaySubject<AssistancePlanView> = new ReplaySubject<AssistancePlanView>();
  @Input() editable: boolean = false;
  @Output() addedValueEvent = new EventEmitter<AssistancePlanHourDto>();
  @Output() updatedValueEvent = new EventEmitter<AssistancePlanHourDto>();
  @Output() deletedValueEvent = new EventEmitter<AssistancePlanHourDto>();

  // CONFIG
  tableColumns = ['type', 'weeklyHours', 'action'];

  // VARs
  hourTypes: HourTypeDto[] = [];
  filteredHourTypes: HourTypeDto[] = [];
  value: AssistancePlanHourDto = new AssistancePlanHourDto();

  // FORMS
  editForm = new UntypedFormGroup({
    type: new UntypedFormControl(null, Validators.compose([Validators.required])),
    weeklyHoursPart: new UntypedFormControl(
      0,
      Validators.compose([
        Validators.min(0),
        Validators.max(9999),
        Validators.required
      ])
    ),
    weeklyMinutesPart: new UntypedFormControl(
      0,
      Validators.compose([
        Validators.min(0),
        Validators.max(59),
        Validators.required
      ])
    )
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
    combineLatest([
      this.hourTypeService.allValues$,
      this.assistancePlanView$
    ])
      .subscribe(([types, plan]) => {
        this.values = plan.dto.hours;
        this.values$.next(this.values);
        this.hourTypes = types;

        this.assistancePlanView = plan;
        this.editable = plan.editable;

        this.filteredTableData = this.convertToTableSource(this.values);

        this.refreshTablePage();
      });

    this.initFormSubscriptions();
  }

  getNewValue(): AssistancePlanHourDto {
    return new AssistancePlanHourDto()
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
    this.typeControl.valueChanges.subscribe(value =>
      this.editValue.hourTypeId = this.hourTypes.find(type => type.id === value)?.id ?? 0);
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
    this.filteredHourTypes =
      this.hourTypes.filter(x => !this.assistancePlanView.dto.hours.some(y => y.hourTypeId == x.id));
  }

  create(value: AssistancePlanHourDto) {
    this.addedValueEvent.emit(value);
    this.loadValues();
  }

  update(value: AssistancePlanHourDto) {
    this.updatedValueEvent.emit(value);
    this.loadValues();
  }

  delete(value: AssistancePlanHourDto) {
    this.deletedValueEvent.emit(value);
    this.loadValues();
  }

  resetHourType() {
    this.typeControl.setValue(null);
  }

  getHourTypeName(value: HourTypeDto | null) {
    if (value == null)
      return "n/a";

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
