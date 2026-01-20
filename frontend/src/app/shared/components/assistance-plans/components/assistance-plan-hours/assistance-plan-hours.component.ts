import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {AssistancePlanHourDto} from "../../../../dtos/assistance-plan-hour-dto.model";
import {HourTypeDto} from "../../../../dtos/hour-type-dto.model";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HourTypeService} from "../../../../services/hour-type.service";
import {combineLatest} from "rxjs";
import {AssistancePlanView} from "../../../../models/assistance-plan-view.model";
import {TablePageComponent} from "../../../table-page.component";
import {HelperService} from "../../../../services/helper.service";
import {Sort} from "@angular/material/sort";

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
    weeklyHours: new UntypedFormControl(
      0,
      Validators.compose([
        Validators.min(0.1),
        Validators.max(9999),
        Validators.required
      ])
    )
  });

  get typeControl() { return this.editForm.controls['type']; }

  get weeklyHoursControl() { return this.editForm.controls['weeklyHours']; }

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
    this.weeklyHoursControl.valueChanges.subscribe(value => this.editValue.weeklyHours = value);
    this.typeControl.valueChanges.subscribe(value =>
      this.editValue.hourTypeId = this.hourTypes.find(type => type.id === value)?.id ?? 0);
  }

  fillEditForm(value: AssistancePlanHourDto) {
    this.weeklyHoursControl.setValue(value.weeklyHours);
    this.typeControl.setValue(value.hourTypeId);
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
}
