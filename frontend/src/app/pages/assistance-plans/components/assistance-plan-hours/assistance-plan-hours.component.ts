import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Sort} from '@angular/material/sort';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {combineLatest, ReplaySubject} from 'rxjs';
import {UntypedFormControl, UntypedFormGroup, Validators} from '@angular/forms';
import {TablePageComponent} from '../../../../shared/components/table-page.component';
import {AssistancePlanHourDto} from '../../../../shared/dtos/assistance-plan-hour-dto.model';
import {HourTypeDto} from '../../../../shared/dtos/hour-type-dto.model';
import {AssistancePlanView} from '../../../../shared/models/assistance-plan-view.model';
import {HelperService} from '../../../../shared/services/helper.service';
import {HourTypeService} from '../../../../shared/services/hour-type.service';

@Component({
  selector: 'app-assistance-plan-hours-page',
  templateUrl: './assistance-plan-hours.component.html',
  styleUrls: ['./assistance-plan-hours.component.css'],
  standalone: false
})
export class AssistancePlanHoursPageComponent
  extends TablePageComponent<AssistancePlanHourDto, [AssistancePlanHourDto, HourTypeDto | null]>
  implements OnInit {

  @Input() assistancePlanView: AssistancePlanView = new AssistancePlanView();
  @Input() assistancePlanView$: ReplaySubject<AssistancePlanView> = new ReplaySubject<AssistancePlanView>();
  @Input() editable = false;
  @Output() addedValueEvent = new EventEmitter<AssistancePlanHourDto>();
  @Output() updatedValueEvent = new EventEmitter<AssistancePlanHourDto>();
  @Output() deletedValueEvent = new EventEmitter<AssistancePlanHourDto>();

  tableColumns = ['type', 'weeklyHours', 'action'];

  hourTypes: HourTypeDto[] = [];
  filteredHourTypes: HourTypeDto[] = [];

  editForm = new UntypedFormGroup({
    type: new UntypedFormControl(null, Validators.compose([Validators.required])),
    weeklyHours: new UntypedFormControl(0, Validators.compose([
      Validators.min(0.1),
      Validators.max(9999),
      Validators.required
    ]))
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
    ]).subscribe(([types, plan]) => {
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
    return new AssistancePlanHourDto();
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
      this.editValue.hourTypeId = this.hourTypes.find(type => type.id === value)?.id ?? 0
    );
  }

  fillEditForm(value: AssistancePlanHourDto) {
    this.weeklyHoursControl.setValue(value.weeklyHours);
    this.typeControl.setValue(value.hourTypeId);
  }

  filterTableData() {
    this.refreshTablePage();
  }

  override filterReferenceValues() {
    this.filteredHourTypes = this.hourTypes.filter(x => !this.assistancePlanView.dto.hours.some(y => y.hourTypeId === x.id));
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
    if (value == null) {
      return 'n/a';
    }

    return value.title;
  }

  sortData(sort: Sort) {
  }
}
