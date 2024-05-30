import { Component, OnInit } from '@angular/core';
import {InstitutionDto} from "../../dtos/institution-dto.model";
import {PermissionDto} from "../../dtos/permission-dto.model";
import {EmployeeDto} from "../../dtos/employee-dto.model";
import {AbstractControl, UntypedFormGroup} from "@angular/forms";
import {EmployeeService} from "../../services/employee.service";
import {InstitutionService} from "../../services/institution.service";
import {InstitutionInfoForm} from "../../shared/form/institution-info-form";
import {NewPageComponent} from "../../shared/modules/new-page.component";
import {HelperService} from "../../services/helper.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-institution-new',
  templateUrl: './institution-new.component.html',
  styleUrls: ['./institution-new.component.css']
})
export class InstitutionNewComponent extends NewPageComponent<InstitutionDto> implements OnInit {

  // configs
  permissions: [InstitutionDto, EmployeeDto, PermissionDto][] = [];
  permissionTableColumns: string[] = ['name', 'lead', 'write', 'read', 'affiliated'];

  infoForm = new InstitutionInfoForm();
  permissionForm = new UntypedFormGroup({ });

  constructor(
    private employeeService: EmployeeService,
    private institutionService: InstitutionService,
    override helperService: HelperService,
    override location: Location) {
    super(helperService, location)
  }

  override ngOnInit() {
    super.ngOnInit();
    this.initFormSubscriptions();
  }

  loadReferenceValues() {
    this.employeeService.allValues$.subscribe({
      next: (data) => {
        if (data.length !== undefined) {
          this.permissions = data.map(x => [
            this.value,
            x,
            <PermissionDto> {
              employeeId: x.id,
              institutionId: 0,
              writeEntries: false,
              readEntries: false,
              changeInstitution: false,
              affiliated: false }
          ]);
        }
      },
      error: () => this.handleFailure("Fehler beim laden", true)
    })
  }

  getNewValue(): InstitutionDto {
    return new InstitutionDto();
  }

  initFormSubscriptions() {
    this.infoForm.name.valueChanges.subscribe(value => this.value.name = value);
    this.infoForm.email.valueChanges.subscribe(value => this.value.email = value);
    this.infoForm.phone.valueChanges.subscribe(value => this.value.phonenumber = value);
  }

  create() {
    if (this.infoForm.invalid || this.permissionForm.invalid || this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;
    this.value.permissions = this.getPermissionDtos();

    // save employee access data
    this.institutionService
      .create(this.value)
      .subscribe({
        next: () => this.handleSuccess("Bereich gespeichert", true),
        error: () => this.handleFailure("Fehler beim speichern")
      });
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

  private getPermissionDtos(): PermissionDto[] {
    return this.permissions.map((permission) =>
      <PermissionDto> {
        employeeId: permission[1].id,
        institutionId: 0,
        writeEntries: permission[2].writeEntries,
        readEntries: permission[2].readEntries,
        changeInstitution: permission[2].changeInstitution,
        affiliated: permission[2].affiliated }
    )
  }
}
