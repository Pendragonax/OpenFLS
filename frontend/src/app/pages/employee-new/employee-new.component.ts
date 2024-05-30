import { Component, OnInit } from '@angular/core';
import {AbstractControl, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {EmployeeService} from "../../services/employee.service";
import {Router} from "@angular/router";
import {STEPPER_GLOBAL_OPTIONS} from "@angular/cdk/stepper";
import {InstitutionService} from "../../services/institution.service";
import {PermissionDto} from "../../dtos/permission-dto.model";
import {EmployeeDto} from "../../dtos/employee-dto.model";
import {InstitutionDto} from "../../dtos/institution-dto.model";
import {NewPageComponent} from "../../shared/modules/new-page.component";
import {Location} from "@angular/common";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-employee-new',
  templateUrl: './employee-new.component.html',
  styleUrls: ['./employee-new.component.css'],
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: {showError: true},
    },
  ],
})
export class EmployeeNewComponent extends NewPageComponent<EmployeeDto> implements OnInit {

  // VARs
  permissions: [InstitutionDto, PermissionDto][] = [];

  // configs
  permissionTableColumns: string[] = ['name', 'lead', 'write', 'read', 'affiliated'];

  // FORMs
  accessForm: UntypedFormGroup = new UntypedFormGroup({
    username: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
      Validators.required,
      Validators.minLength(6)])),
    role: new UntypedFormControl({value:'3', disabled: false})
  });
  personalInfoForm = new UntypedFormGroup({
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
    description: new UntypedFormControl(''),
    inactive: new UntypedFormControl('')
  });
  permissionForm = new UntypedFormGroup({
  })

  get usernameControl() { return this.accessForm.controls['username']; }

  get roleControl() { return this.accessForm.controls['role']; }

  get firstNameControl() { return this.personalInfoForm.controls['firstName']; }

  get lastNameControl() { return this.personalInfoForm.controls['lastName']; }

  get phoneControl() { return this.personalInfoForm.controls['phone']; }

  get emailControl() { return this.personalInfoForm.controls['email']; }

  get descriptionControl() { return this.personalInfoForm.controls['description']; }

  get inactiveControl() { return this.personalInfoForm.controls['inactive']; }

  constructor(
    private employeeService: EmployeeService,
    private institutionService: InstitutionService,
    override helperService: HelperService,
    override location: Location,
    private router: Router) {
    super(helperService, location);
  }

  override ngOnInit() {
    super.ngOnInit();
    this.initFormSubscriptions();
  }

  getNewValue(): EmployeeDto {
    return new EmployeeDto();
  }

  loadReferenceValues() {
    this.institutionService.allValues$.subscribe({
      next: (data) => {
        if (data.length !== undefined) {
          this.permissions = data.map(x => [
            x,
            <PermissionDto> {
              employeeId: 0,
              institutionId: x.id,
              writeEntries: false,
              readEntries: false,
              changeInstitution: false,
              affiliated: false }
          ]);
        }
      },
      error: () => this.router.navigate(["/employees"]).then()
    })
  }

  initFormSubscriptions() {
    this.usernameControl.valueChanges.subscribe(value => {
      if (this.value.access != null)
        this.value.access.username = value
    });
    this.roleControl.valueChanges.subscribe(value => {
      if (this.value.access != null)
        this.value.access.role = value
    });
    this.firstNameControl.valueChanges.subscribe(value => this.value.firstName = value);
    this.lastNameControl.valueChanges.subscribe(value => this.value.lastName = value);
    this.phoneControl.valueChanges.subscribe(value => this.value.phonenumber = value);
    this.emailControl.valueChanges.subscribe(value => this.value.email = value);
    this.descriptionControl.valueChanges.subscribe(value => this.value.description = value);
    this.inactiveControl.valueChanges.subscribe(value => this.value.inactive = value);
  }

  private getPermissionDtos(): PermissionDto[] {
    return this.permissions.map((permission) =>
      <PermissionDto> {
        employeeId: 0,
        institutionId: permission[0].id,
        writeEntries: permission[1].writeEntries,
        readEntries: permission[1].readEntries,
        changeInstitution: permission[1].changeInstitution,
        affiliated: permission[1].affiliated }
    )
  }

  create() {
    if (this.accessForm.invalid || this.personalInfoForm.invalid || this.isSubmitting) {
        return;
    }

    this.isSubmitting = true;
    this.value.permissions = this.getPermissionDtos();

    this.employeeService
      .create(this.value)
      .subscribe({
        next: () => this.handleSuccess("Mitareiter hinzugefügt!", true),
        error: err => this.handleFailure(err)
      });
  }

  getControlErrorMessage(control: AbstractControl | null) {
    if (control?.hasError('required')) { return 'Eingabe ist notwendig'; }
    if (control?.hasError('minlength')) { return 'zu wenig Zeichen'; }
    if (control?.hasError('pattern')) { return 'Großbuchtabe, Zahl und Sonderzeichen notwendig'; }

    return 'unbekannter Fehler';
  }

  getAccessFormErrorMessage() {
    return "unvollständig";
  }

  getPersonalInfoErrorMessage() {
    return "unvollständig";
  }
}
