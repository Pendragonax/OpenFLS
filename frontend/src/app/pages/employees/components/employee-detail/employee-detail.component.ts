import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {EmployeeService} from "../../../../shared/services/employee.service";
import {EmployeeDto} from "../../../../shared/dtos/employee-dto.model";
import {InstitutionService} from "../../../../shared/services/institution.service";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {PermissionDto} from "../../../../shared/dtos/permission-dto.model";
import {ReplaySubject, Subject} from "rxjs";
import {InstitutionDto} from "../../../../shared/dtos/institution-dto.model";
import {UserService} from "../../../../shared/services/user.service";
import {DtoCombinerService} from "../../../../shared/services/dto-combiner.service";
import {combineLatest} from "rxjs";
import {UnprofessionalDto} from "../../../../shared/dtos/unprofessional-dto.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HelperService} from "../../../../shared/services/helper.service";
import {DetailPageComponent} from "../../../../shared/components/detail-page.component";
import {EmployeeViewModel} from "../../../../shared/models/employee-view.model";

@Component({
    selector: 'app-employee-detail',
    templateUrl: './employee-detail.component.html',
    styleUrls: ['./employee-detail.component.css'],
    standalone: false
})
export class EmployeeDetailComponent extends DetailPageComponent<EmployeeDto> implements OnInit {
  // VARs
  notProfessionals$: Subject<UnprofessionalDto[]> = new Subject<UnprofessionalDto[]>();
  permissions: [InstitutionDto, EmployeeDto, PermissionDto][] = [];
  permissionTableColumns: string[] = ['name', 'lead', 'write', 'read', 'affiliated'];
  employeeView$: ReplaySubject<EmployeeViewModel> = new ReplaySubject<EmployeeViewModel>();
  employee$: ReplaySubject<EmployeeDto> = new ReplaySubject<EmployeeDto>();

  // STATEs
  editMode: boolean = false;
  adminMode: boolean = false;

  // FORMs
  permissionForm = new UntypedFormGroup({
    role: new UntypedFormControl()
  });
  detailForm = new UntypedFormGroup({
    firstName: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
      Validators.required,
      Validators.minLength(1)])),
    lastName: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
      Validators.required,
      Validators.minLength(1)])),
    phone: new UntypedFormControl(''),
    email: new UntypedFormControl('', Validators.compose([
      Validators.pattern("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")
    ])),
    description: new UntypedFormControl('')}
  );

  get roleControl() { return this.permissionForm.controls['role']; }

  get firstNameControl() { return this.detailForm.controls['firstName']; }

  get lastNameControl() { return this.detailForm.controls['lastName']; }

  get phoneControl() { return this.detailForm.controls['phone']; }

  get emailControl() { return this.detailForm.controls['email']; }

  get descriptionControl() { return this.detailForm.controls['description']; }

  constructor(
    private employeeService: EmployeeService,
    private institutionService: InstitutionService,
    private route: ActivatedRoute,
    private userService: UserService,
    private dtoCombinerService: DtoCombinerService,
    protected override helperService: HelperService,
    private modal: NgbModal
  ) {
    super(helperService);
  }

  loadValues() {
    // get id
    const id = this.route.snapshot.paramMap.get('id');

    if (id != null) {
      combineLatest([
        this.employeeService.getById(+id),
        this.institutionService.allValues$,
        this.userService.leadingInstitutions$,
        this.userService.user$
      ])
        .subscribe(([employee, institutions, leadingIds, user]) => {
          this.value = employee;
          this.value.access = null;
          this.value$.next(employee);
          this.editValue = <EmployeeDto> {...employee};
          this.editValue.access = null;
          this.notProfessionals$.next(employee.unprofessionals);
          this.adminMode = (user?.access?.role ?? 99) === 1;
          this.editMode = this.adminMode ||
            leadingIds.some(x => employee.permissions.some(y => y.affiliated && y.institutionId == x));
          this.permissions = this.dtoCombinerService.combinePermissionsByEmployee(employee, institutions);

          if (this.editMode) {
            this.employee$.next(this.value);
          }
          this.employeeView$.next(<EmployeeViewModel> {
            dto: this.value,
            editable: this.editMode
          });

          this.refreshForm();
        });
    }
  }

  refreshForm() {
    this.firstNameControl.setValue(this.value.firstName);
    this.lastNameControl.setValue(this.value.lastName);
    this.phoneControl.setValue(this.value.phonenumber);
    this.emailControl.setValue(this.value.email);
    this.descriptionControl.setValue(this.value.description);
    this.roleControl.setValue(this.value.access?.role);
  }

  getNewValue(): EmployeeDto {
    return new EmployeeDto();
  }

  initFormSubscriptions() {
    this.firstNameControl.valueChanges.subscribe(value => this.editValue.firstName = value);
    this.lastNameControl.valueChanges.subscribe(value => this.editValue.lastName = value);
    this.phoneControl.valueChanges.subscribe(value => this.editValue.phonenumber = value);
    this.emailControl.valueChanges.subscribe(value => this.editValue.email = value);
    this.descriptionControl.valueChanges.subscribe(value => this.editValue.description = value);
    this.roleControl.valueChanges.subscribe(value => {
      if (this.editValue.access != null) {
        this.editValue.access.role = value
      }
    });
  }

  update() {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.employeeService.update(this.editValue.id, this.editValue).subscribe({
      next: () => this.handleSuccess("Mitarbeiter geändert"),
      error: () => this.handleFailure("Fehler beim speichern")
    })
  }

  savePermissions() {
    this.editValue.access = null;

    this.editValue.permissions = this.permissions.map(permission => <PermissionDto> {
      institutionId: permission[0].id,
      readEntries: permission[2].readEntries,
      writeEntries: permission[2].writeEntries,
      changeInstitution: permission[2].changeInstitution,
      affiliated: permission[2].affiliated
    } );

    this.update();
  }

  updateRole() {
    if (this.value.id == null || this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.employeeService.updateRole(this.value?.id, this.permissionForm.get('role')?.value).subscribe({
      next: () => this.handleSuccess("Rolle geändert"),
      error: () => this.handleFailure("Fehler beim speichern der Rolle"),
    });
  }

  updateInactiveState(inactive: boolean) {
    this.editValue.inactive = inactive;
    this.update();
  }

  resetPassword() {
    if (this.value.id == null || this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.employeeService.resetPassword(this.value.id).subscribe({
      next: () => this.handleSuccess("Passwort zurückgesetzt"),
      error: () => this.handleFailure("Fehler beim zurücksetzen"),
    })
  }

  createNotProfessional(value: UnprofessionalDto) {
    this.editValue.unprofessionals.push(value);
    this.update();
  }

  updateNotProfessional(value: UnprofessionalDto) {
    const tmpNotProf = this.editValue.unprofessionals
      .find(x => x.employeeId == value.employeeId && x.sponsorId == value.sponsorId);

    if (tmpNotProf != null) {
      tmpNotProf.end = value.end;
      this.update();
    }
  }

  deleteNotProfessional(value: UnprofessionalDto) {
    this.editValue.unprofessionals = this.editValue.unprofessionals
      .filter(x => x.employeeId != value.employeeId && x.sponsorId != value.sponsorId);

    this.update();
  }

  openPasswordResetModal(content) {
    this.modal
      .open(content, { ariaLabelledBy: 'modal-password-reset-title', scrollable: true })
      .result
      .then(result => {
        if (result) {
          this.resetPassword();
        }
      });
  }
}
