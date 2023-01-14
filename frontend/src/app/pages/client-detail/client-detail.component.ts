import { Component, OnInit } from '@angular/core';
import {InstitutionService} from "../../services/institution.service";
import {CategoriesService} from "../../services/categories.service";
import {ClientsService} from "../../services/clients.service";
import {ActivatedRoute} from "@angular/router";
import {InstitutionDto} from "../../dtos/institution-dto.model";
import {CategoryTemplateDto} from "../../dtos/category-template-dto.model";
import {combineLatest} from "rxjs";
import {UserService} from "../../services/user.service";
import {ClientView} from "../../models/client-view.model";
import {DetailPageComponent} from "../../shared/modules/detail-page.component";
import {HelperService} from "../../services/helper.service";
import {ClientInformationForm} from "../../shared/form/client-information-form";
import {ClientDto} from "../../dtos/client-dto.model";

@Component({
  selector: 'app-client-detail',
  templateUrl: './client-detail.component.html',
  styleUrls: ['./client-detail.component.css']
})
export class ClientDetailComponent extends DetailPageComponent<ClientView> implements OnInit {

  // VARs
  institutions: InstitutionDto[] = [];
  categoryTemplates: CategoryTemplateDto[] = [];

  // STATEs
  editMode = false;

  infoForm = new ClientInformationForm();

  constructor(
    override helperService: HelperService,
    private institutionService: InstitutionService,
    private categoryTemplateService: CategoriesService,
    private clientService: ClientsService,
    private route: ActivatedRoute,
    private userService: UserService,) {
    super(helperService);
  }

  loadValues() {
    // get id
    const id = this.route.snapshot.paramMap.get('id');

    if (id != null) {
      combineLatest([
        this.clientService.getById(+id),
        this.institutionService.allValues$,
        this.categoryTemplateService.allValues$,
        this.userService.user$
      ])
        .subscribe(([client, institutions, categories, user]) => {
          this.value.dto = client;
          this.value.editable = user.access?.role === 1 || user.permissions
            .filter(perm => perm.affiliated)
            .some(perm => perm.institutionId === client.institution.id);
          this.value$.next(this.value);
          this.editValue = <ClientView> {...this.value};
          this.institutions = institutions;
          this.categoryTemplates = categories;
          this.editMode = user.institutionId == this.value.dto.institution.id;

          this.refreshForm();
        });
    }
  }

  refreshForm() {
    this.infoForm.firstName.setValue(this.value.dto.firstName);
    this.infoForm.lastName.setValue(this.value.dto.lastName);
    this.infoForm.phone.setValue(this.value.dto.phoneNumber);
    this.infoForm.email.setValue(this.value.dto.email);
    this.infoForm.institution.setValue(this.value.dto.institution.id);
    this.infoForm.categoryTemplate.setValue(this.value.dto.categoryTemplate.id);
  }

  getNewValue(): ClientView {
    return new ClientView();
  }

  initFormSubscriptions() {
    this.infoForm.firstName.valueChanges.subscribe(value => this.editValue.dto.firstName = value);
    this.infoForm.lastName.valueChanges.subscribe(value => this.editValue.dto.lastName = value);
    this.infoForm.phone.valueChanges.subscribe(value => this.editValue.dto.phoneNumber = value);
    this.infoForm.email.valueChanges.subscribe(value => this.editValue.dto.email = value);
    this.infoForm.institution.valueChanges.subscribe(value => {
      const selectedInstitution = this.institutions.find(institution => institution.id === value);
      if (selectedInstitution != null) {
        this.editValue.dto.institution = selectedInstitution
      }
    });
    this.infoForm.categoryTemplate.valueChanges.subscribe(value => {
      const selectedTemplate = this.categoryTemplates.find(template => template.id === value);
      if (selectedTemplate != null) {
        this.editValue.dto.categoryTemplate = selectedTemplate;
      }
    });
  }

  update() {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.clientService.update(this.editValue.dto.id, this.editValue.dto).subscribe({
      next: () => this.handleSuccess("Klient geändert"),
      error: () => this.handleFailure("Fehler beim speichern")
    })
  }
}
