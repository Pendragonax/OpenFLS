import { Component, OnInit } from '@angular/core';
import {UserService} from "../../shared/services/user.service";
import {combineLatest, ReplaySubject} from "rxjs";
import {EmployeeDto} from "../../shared/dtos/employee-dto.model";
import {AbstractControl, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {PasswordDto} from "../../shared/dtos/password-dto.model";
import {InstitutionService} from "../../shared/services/institution.service";
import {PermissionDto} from "../../shared/dtos/permission-dto.model";
import {DtoCombinerService} from "../../shared/services/dto-combiner.service";
import {InstitutionDto} from "../../shared/dtos/institution-dto.model";
import {HelperService} from "../../shared/services/helper.service";
import {createMatchingPasswordsValidator} from "../../shared/validators/matching-passwords.validator";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    standalone: false
})
export class HomeComponent implements OnInit {

  tableColumns: string[] = ['name', 'lead', 'write', 'read', 'affiliated'];
  pwdPattern = '(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&].{8,}';
  favorite$: ReplaySubject<Boolean> = new ReplaySubject<Boolean>();

  currentEmployee: EmployeeDto = new EmployeeDto();
  currentEmployee$: ReplaySubject<EmployeeDto> = new ReplaySubject<EmployeeDto>();
  permissions: [InstitutionDto, EmployeeDto, PermissionDto][] = [];
  role = "";
  username: string = "";
  passwordDto: PasswordDto = new PasswordDto();

  isSubmitting = false;

  // FORMs
  passwordForm = new UntypedFormGroup({
    oldPassword: new UntypedFormControl({value:'', disabled: false}),
    password1: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
      Validators.required,
      Validators.pattern(this.pwdPattern),
      Validators.minLength(8)])),
    password2: new UntypedFormControl({value:'', disabled: false})
  }, { validators: createMatchingPasswordsValidator });

  get oldPasswordControl() { return this.passwordForm.controls['oldPassword']; }

  get password1Control() { return this.passwordForm.controls['password1']; }

  get password2Control() { return this.passwordForm.controls['password2']; }

  constructor(
    private helperService: HelperService,
    private userService: UserService,
    private institutionService: InstitutionService,
    private dtoCombinerService: DtoCombinerService
  ) {  }

  ngOnInit(): void {
    this.loadValues();
    this.initFormSubscriptions();
    this.favorite$.next(true);
  }

  loadValues() {
    combineLatest([
      this.userService.user$,
      this.institutionService.allValues$]
    )
      .subscribe(([employee, institutions]) => {
        this.currentEmployee = employee;
        this.currentEmployee$.next(employee);
        this.username = employee.access?.username ?? "";
        this.role = HomeComponent.getRole(employee.access?.role);
        this.permissions = institutions != null ?
          this.dtoCombinerService.combinePermissionsByEmployee(employee, institutions) : [];
      })
  }

  refreshUser() {
    this.userService.loadUser();
  }

  initFormSubscriptions() {
    this.oldPasswordControl.valueChanges.subscribe(value => this.passwordDto.oldPassword = value);
    this.password1Control.valueChanges.subscribe(value => this.passwordDto.newPassword = value);
  }

  resetPasswordForm() {
    this.oldPasswordControl.setValue("");
    this.password1Control.setValue("");
    this.password2Control.setValue("");
  }

  updatePassword() {
    if (this.isSubmitting || this.passwordForm.invalid)
      return;

    this.isSubmitting = true;

    this.userService.changePassword(this.passwordDto).subscribe({
      next: () => {
        this.helperService.openSnackBar("Passwort erfolgreich geändert!");
        this.resetPasswordForm();
        this.isSubmitting = false;
      },
      error: () => {
        this.helperService.openSnackBar("Passwort konnte nicht geändert werden!");
        this.isSubmitting = false;
      }
    })
  }

  getControlErrorMessage(control: AbstractControl | null) {
    if (control?.hasError('required')) {
      return 'Eingabe ist notwendig';
    }
    if (control?.hasError('minlength')) {
      return 'zu wenig Zeichen';
    }
    if (control?.hasError('pattern')) {
      return 'Großbuchtabe, Zahl und Sonderzeichen notwendig';
    }

    return 'unbekannter Fehler';
  }

  private static getRole(role: number | undefined): string {
    if (role === undefined)
      return "Mitarbeiter";

    switch(role) {
      case 1:
        return "Administrator";
      case 2:
        return "Leitungskraft";
      default:
        return "Mitarbeiter"
    }
  }
}
