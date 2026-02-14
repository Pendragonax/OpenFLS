import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
  MAT_NATIVE_DATE_FORMATS,
  NativeDateAdapter
} from '@angular/material/core';
import {Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {AbstractControl, UntypedFormControl, UntypedFormGroup, ValidationErrors, Validators} from '@angular/forms';
import {TablePageComponent} from '../../../../shared/components/table-page.component';
import {GoalDto} from '../../../../shared/dtos/goal-dto.model';
import {GoalHourDto} from '../../../../shared/dtos/goal-hour-dto.model';
import {HourTypeDto} from '../../../../shared/dtos/hour-type-dto.model';
import {InstitutionDto} from '../../../../shared/dtos/institution-dto.model';
import {HelperService} from '../../../../shared/services/helper.service';
import {HourTypeService} from '../../../../shared/services/hour-type.service';
import {InstitutionService} from '../../../../shared/services/institution.service';
import {
  AssistancePlanCreateGoalDto,
  AssistancePlanCreateHourDto
} from '../../../../shared/dtos/assistance-plan-create-dto.model';

const weeklyDurationValidator = (control: AbstractControl): ValidationErrors | null => {
  const hours = Number(control.get('weeklyHoursPart')?.value ?? 0);
  const minutes = Number(control.get('weeklyMinutesPart')?.value ?? 0);

  if (!Number.isFinite(hours) || !Number.isFinite(minutes)) {
    return {weeklyDurationInvalid: true};
  }

  if (hours < 0 || hours > 999 || minutes < 0 || minutes > 59) {
    return {weeklyDurationInvalid: true};
  }

  return (hours * 60 + minutes) > 0 ? null : {weeklyDurationRequired: true};
};

@Component({
  selector: 'app-assistance-plan-goals',
  templateUrl: './assistance-plan-goals.component.html',
  styleUrls: ['./assistance-plan-goals.component.css'],
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
export class AssistancePlanGoalsComponent extends TablePageComponent<GoalDto, [GoalDto, InstitutionDto | null]> implements OnInit {
  @Input() editable = false;
  @Input() set goals(value: AssistancePlanCreateGoalDto[]) {
    this.values = (value ?? []).map((goal, index) => this.toGoalDto(goal, index + 1));
    this.filteredTableData = this.convertToTableSource(this.values);
    this.refreshTablePage();
  }

  @Output() goalsChange = new EventEmitter<AssistancePlanCreateGoalDto[]>();

  tableColumns = ['title', 'description', 'institution', 'weeklyHours', 'action'];
  hourTableColumns = ['type', 'weeklyHours', 'actions'];

  deleteServiceCount = 0;

  institutions: InstitutionDto[] = [];
  hourTypes: HourTypeDto[] = [];
  filteredHourTypes: HourTypeDto[] = [];
  editGoalHour: GoalHourDto = new GoalHourDto();
  hourTableSource: MatTableDataSource<[GoalHourDto, HourTypeDto]> = new MatTableDataSource();
  emptyHourTableSource = true;

  infoForm = new UntypedFormGroup({
    title: new UntypedFormControl('', Validators.compose([Validators.required, Validators.minLength(1), Validators.maxLength(64)])),
    description: new UntypedFormControl('', Validators.compose([Validators.maxLength(256), Validators.required])),
    institution: new UntypedFormControl(null)
  });

  hourForm = new UntypedFormGroup({
    hourType: new UntypedFormControl(null, Validators.compose([Validators.required])),
    weeklyHoursPart: new UntypedFormControl(0, Validators.compose([Validators.required, Validators.min(0), Validators.max(999)])),
    weeklyMinutesPart: new UntypedFormControl(0, Validators.compose([Validators.required, Validators.min(0), Validators.max(59)])),
    hourTable: new UntypedFormControl()
  }, {validators: weeklyDurationValidator});

  get titleControl() { return this.infoForm.controls['title']; }

  get descriptionControl() { return this.infoForm.controls['description']; }

  get institutionControl() { return this.infoForm.controls['institution']; }

  get hourTypeControl() { return this.hourForm.controls['hourType']; }

  get weeklyHoursPartControl() { return this.hourForm.controls['weeklyHoursPart']; }

  get weeklyMinutesPartControl() { return this.hourForm.controls['weeklyMinutesPart']; }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private institutionService: InstitutionService,
    private hourTypeService: HourTypeService
  ) {
    super(modalService, helperService);
  }

  loadValues() {
    this.institutionService.allValues$.subscribe(institutions => {
      this.institutions = institutions;
      this.filteredTableData = this.convertToTableSource(this.values);
      this.refreshTablePage();
    });

    this.hourTypeService.allValues$.subscribe(types => {
      this.hourTypes = types;
      this.filteredHourTypes = this.hourTypes;
      this.refreshGoalHoursTableSource();
      this.filteredTableData = this.convertToTableSource(this.values);
      this.refreshTablePage();
    });
  }

  getNewValue(): GoalDto {
    return new GoalDto();
  }

  convertToTableSource(values: GoalDto[]): [GoalDto, InstitutionDto | null][] {
    return values.map(value => [
      value,
      this.institutions.find(type => type.id === value.institutionId) ?? null
    ]);
  }

  initFormSubscriptions() {
    this.titleControl.valueChanges.subscribe(value => this.editValue.title = value);
    this.descriptionControl.valueChanges.subscribe(value => this.editValue.description = value);
    this.institutionControl.valueChanges.subscribe(value =>
      this.editValue.institutionId = this.institutions.find(institution => institution.id === value)?.id ?? null
    );

    this.hourTypeControl.valueChanges.subscribe(value => this.editGoalHour.hourTypeId = value != null ? value : 0);
    this.weeklyHoursPartControl.valueChanges.subscribe(() => this.syncGoalWeeklyHoursFromParts());
    this.weeklyMinutesPartControl.valueChanges.subscribe(() => this.syncGoalWeeklyHoursFromParts());
  }

  fillEditForm(value: GoalDto) {
    this.titleControl.setValue(value.title);
    this.descriptionControl.setValue(value.description);
    this.institutionControl.setValue(value.institutionId);

    this.resetGoalHoursForm();
    this.refreshGoalHoursTableSource();
  }

  resetGoalHoursForm() {
    this.hourTypeControl.setValue(null);
    this.weeklyHoursPartControl.setValue(0);
    this.weeklyMinutesPartControl.setValue(0);
    this.syncGoalWeeklyHoursFromParts();
  }

  filterTableData() {
    this.refreshTablePage();
  }

  override filterReferenceValues() {
    this.filteredHourTypes = this.hourTypes.filter(x => !this.editValue.hours.some(y => y.hourTypeId === x.id));
  }

  refreshGoalHoursTableSource() {
    this.hourTableSource.data = this.editValue.hours.map(value => {
      return [
        value,
        this.hourTypes.find(type => type.id === value.hourTypeId) ?? new HourTypeDto()
      ];
    });

    this.emptyHourTableSource = this.hourTableSource.data.length <= 0;
  }

  create(goal: GoalDto) {
    const withId = {
      ...goal,
      id: this.values.length > 0 ? Math.max(...this.values.map(v => v.id)) + 1 : 1
    };
    this.values = [...this.values, withId];
    this.emitGoals();
  }

  update(goal: GoalDto) {
    this.values = this.values.map(current => current.id === goal.id ? {...goal} : current);
    this.emitGoals();
  }

  delete(goal: GoalDto) {
    this.values = this.values.filter(current => current.id !== goal.id);
    this.emitGoals();
  }

  createGoalHour() {
    if (this.editGoalHour.hourTypeId <= 0 || this.editGoalHour.weeklyHours <= 0) {
      return;
    }

    this.editValue.hours.push({...this.editGoalHour});
    this.editGoalHour = new GoalHourDto();

    this.filterReferenceValues();
    this.resetGoalHoursForm();
    this.refreshGoalHoursTableSource();
  }

  deleteGoalHour(goalHour: GoalHourDto) {
    this.editValue.hours = this.editValue.hours.filter(value => JSON.stringify(value) !== JSON.stringify(goalHour));

    this.filterReferenceValues();
    this.refreshGoalHoursTableSource();
  }

  resetInstitutionControl() {
    this.institutionControl.setValue(null);
  }

  getInstitutionName(institution: InstitutionDto | null) {
    if (institution === null) {
      return 'n/a';
    }

    return institution.name;
  }

  sumWeeklyHours(goal: GoalDto): number {
    if (goal.hours == null || goal.hours.length <= 0) {
      return 0;
    }

    return goal.hours
      .map(value => value.weeklyHours)
      .reduce((sum, current) => sum + current);
  }

  sortData(sort: Sort) {
  }

  clampGoalWeeklyDuration() {
    const safeHours = this.toBoundedInt(this.weeklyHoursPartControl.value, 0, 999);
    const safeMinutes = this.toBoundedInt(this.weeklyMinutesPartControl.value, 0, 59);
    this.weeklyHoursPartControl.setValue(safeHours);
    this.weeklyMinutesPartControl.setValue(safeMinutes);
    this.syncGoalWeeklyHoursFromParts();
  }

  selectAll(event: FocusEvent) {
    const target = event.target as HTMLInputElement | null;
    if (!target) {
      return;
    }
    target.select();
  }

  private emitGoals() {
    this.filteredTableData = this.convertToTableSource(this.values);
    this.refreshTablePage();
    this.goalsChange.emit(this.values.map(goal => ({
      title: goal.title,
      description: goal.description,
      assistancePlanId: goal.assistancePlanId,
      institutionId: goal.institutionId,
      hours: goal.hours.map((hour): AssistancePlanCreateHourDto => ({
        weeklyHours: hour.weeklyHours,
        hourTypeId: hour.hourTypeId
      }))
    })));
  }

  private toGoalDto(goal: AssistancePlanCreateGoalDto, id: number): GoalDto {
    return {
      id,
      title: goal.title,
      description: goal.description,
      assistancePlanId: goal.assistancePlanId,
      institutionId: goal.institutionId,
      hours: (goal.hours ?? []).map((hour, index) => ({
        id: index + 1,
        weeklyHours: hour.weeklyHours,
        goalHourId: 0,
        hourTypeId: hour.hourTypeId
      }))
    };
  }

  private syncGoalWeeklyHoursFromParts() {
    const hoursPart = this.toBoundedInt(this.weeklyHoursPartControl.value, 0, 999);
    const minutesPart = this.toBoundedInt(this.weeklyMinutesPartControl.value, 0, 59);
    const totalMinutes = hoursPart * 60 + minutesPart;
    this.editGoalHour.weeklyHours = Number((totalMinutes / 60).toFixed(4));
  }

  private toBoundedInt(value: unknown, min: number, max: number) {
    const parsed = Number(value ?? 0);
    if (!Number.isFinite(parsed)) {
      return min;
    }
    return Math.max(min, Math.min(max, Math.floor(parsed)));
  }
}
