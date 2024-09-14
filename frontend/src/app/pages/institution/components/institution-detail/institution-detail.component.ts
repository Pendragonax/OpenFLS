import {Component, OnInit} from '@angular/core';
import {EmployeeDto} from "../../../../shared/dtos/employee-dto.model";
import {InstitutionDto} from "../../../../shared/dtos/institution-dto.model";
import {PermissionDto} from "../../../../shared/dtos/permission-dto.model";
import {UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {EmployeeService} from "../../../../shared/services/employee.service";
import {InstitutionService} from "../../../../shared/services/institution.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DtoCombinerService} from "../../../../shared/services/dto-combiner.service";
import {combineLatest, ReplaySubject} from "rxjs";
import {UserService} from "../../../../shared/services/user.service";
import {InstitutionViewModel} from "../../../../shared/models/institution-view.model";
import {InstitutionInfoForm} from "../../forms/institution-info-form";
import {DetailPageComponent} from "../../../../shared/components/detail-page.component";
import {HelperService} from "../../../../shared/services/helper.service";
import {ServiceService} from "../../../../shared/services/service.service";
import {Service} from "../../../../shared/dtos/service.projection";

@Component({
  selector: 'app-institution-detail',
  templateUrl: './institution-detail.component.html',
  styleUrls: ['./institution-detail.component.css']
})
export class InstitutionDetailComponent extends DetailPageComponent<InstitutionViewModel> implements OnInit {
  // VARs
  tableColumns: string[] = ['name', 'lead', 'write', 'read', 'affiliated'];
  permissions: [InstitutionDto, EmployeeDto, PermissionDto][] = [];
  institution$: ReplaySubject<InstitutionDto> = new ReplaySubject<InstitutionDto>();

  editMode: boolean = false;
  adminMode: boolean = false;
  institutionId: number = 0;
  illegalServices: Service[] = []

  permissionForm = new UntypedFormGroup({
    role: new UntypedFormControl()
  })
  infoForm = new InstitutionInfoForm();

  constructor(
    private employeeService: EmployeeService,
    private institutionService: InstitutionService,
    private dtoCombinerService: DtoCombinerService,
    private userService: UserService,
    private serviceService: ServiceService,
    private router: Router,
    override helperService: HelperService,
    private route: ActivatedRoute) {
    super(helperService);
  }

  loadValues() {
    // get id
    const id = this.route.snapshot.paramMap.get('id');

    if (id != null) {
      this.institutionId = parseInt(id)
      // sync loading employees and institution
      combineLatest([
          this.institutionService.getById(+id),
          this.employeeService.allValues$,
          this.userService.leadingInstitutions$,
          this.userService.user$,
          this.serviceService.getIllegalByInstitutionId(+id)
        ]
      )
        .subscribe(([institution, employees, leadingIds, user, illegalServices]) => {
          this.adminMode = (user?.access?.role ?? 99) === 1;
          this.editMode = this.isEditable(leadingIds, institution) || this.adminMode;
          this.value = <InstitutionViewModel>{
            dto: institution,
            editable: this.editMode
          };
          this.value$.next(this.value);
          this.editValue = <InstitutionViewModel>{...this.value};
          this.institution$.next(this.value.dto);
          this.illegalServices = illegalServices;

          // create permission[] from institutions and employees permissions
          if (employees.length !== undefined) {
            this.permissions = this.dtoCombinerService.combinePermissionsByInstitution(institution, employees)
          }

          this.refreshForm();
        })
    }
  }

  refreshForm() {
    this.infoForm.name.setValue(this.editValue.dto.name);
    this.infoForm.email.setValue(this.editValue.dto.email);
    this.infoForm.phone.setValue(this.editValue.dto.phonenumber);
  }

  getNewValue(): InstitutionViewModel {
    return new InstitutionViewModel();
  }

  initFormSubscriptions() {
    this.infoForm.name.valueChanges.subscribe(value => this.editValue.dto.name = value);
    this.infoForm.email.valueChanges.subscribe(value => this.editValue.dto.email = value);
    this.infoForm.phone.valueChanges.subscribe(value => this.editValue.dto.phonenumber = value);
  }

  update() {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.institutionService.update(this.editValue.dto.id, this.editValue.dto).subscribe({
      next: () => this.handleSuccess("Bereich gespeichert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  updatePermissions() {
    this.editValue.dto.permissions = this.permissions.map(permission => <PermissionDto>{
      institutionId: permission[0].id,
      employeeId: permission[1].id,
      changeInstitution: permission[2].changeInstitution,
      readEntries: permission[2].readEntries,
      writeEntries: permission[2].writeEntries,
      affiliated: permission[2].affiliated
    });

    this.update();
  }

  private isEditable(leadingIds: number[], institution: InstitutionDto | null): boolean {
    return leadingIds?.some(id => institution?.id === id) ?? false;
  }
}
