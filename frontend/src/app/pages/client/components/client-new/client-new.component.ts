import { Component, OnInit } from '@angular/core';
import {ClientDto} from "../../../../shared/dtos/client-dto.model";
import {InstitutionDto} from "../../../../shared/dtos/institution-dto.model";
import {CategoryTemplateDto} from "../../../../shared/dtos/category-template-dto.model";
import {InstitutionService} from "../../../../shared/services/institution.service";
import {CategoriesService} from "../../../../shared/services/categories.service";
import {ClientsService} from "../../../../shared/services/clients.service";
import {ClientInformationForm} from "../../forms/client-information-form";
import {HelperService} from "../../../../shared/services/helper.service";
import {NewPageComponent} from "../../../../shared/components/new-page.component";
import {Location} from "@angular/common";

@Component({
  selector: 'app-client-new',
  templateUrl: './client-new.component.html',
  styleUrls: ['./client-new.component.css']
})
export class ClientNewComponent extends NewPageComponent<ClientDto> implements OnInit {
  // VARs
  institutions: InstitutionDto[] = [];
  categoryTemplates: CategoryTemplateDto[] = [];

  infoForm = new ClientInformationForm();

  constructor(
    private institutionService: InstitutionService,
    private categoryTemplateService: CategoriesService,
    private clientService: ClientsService,
    override location: Location,
    override helperService: HelperService
  ) {
    super(helperService, location);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.initFormSubscriptions();
  }

  getNewValue(): ClientDto {
    return new ClientDto();
  }

  loadReferenceValues() {
    this.institutionService.getAll().subscribe({
      next: (values) => this.institutions = values
    });

    this.categoryTemplateService.getAll().subscribe({
      next: (values) => this.categoryTemplates = values
    });
  }

  initFormSubscriptions() {
    this.infoForm.firstName.valueChanges.subscribe((value) => this.value.firstName = value);
    this.infoForm.lastName.valueChanges.subscribe((value) => this.value.lastName = value);
    this.infoForm.phone.valueChanges.subscribe((value) => this.value.phoneNumber = value);
    this.infoForm.email.valueChanges.subscribe((value) => this.value.email = value);
    this.infoForm.institution.valueChanges.subscribe((value) => {
      const selectedInstitution = this.institutions.find(institution => institution.id === value);

      if (selectedInstitution != null)
        this.value.institution = selectedInstitution;
    });
    this.infoForm.categoryTemplate.valueChanges.subscribe((value) => {
      const selectedTemplate = this.categoryTemplates.find(template => template.id === value);

      if (selectedTemplate != null)
        this.value.categoryTemplate = selectedTemplate;
    });
  }

  create() {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.clientService.create(this.value).subscribe({
      next: () => this.handleSuccess("Klient gepspeichert", true),
      error: () => this.handleFailure("Fehler beim speichern")
    })
  }
}
