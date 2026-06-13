import {Component, OnInit} from '@angular/core';
import {InstitutionService} from "../../../../shared/services/institution.service";
import {CategoriesService} from "../../../../shared/services/categories.service";
import {ClientsService} from "../../../../shared/services/clients.service";
import {ActivatedRoute, Router} from "@angular/router";
import {InstitutionDto} from "../../../../shared/dtos/institution-dto.model";
import {CategoryTemplateDto} from "../../../../shared/dtos/category-template-dto.model";
import {combineLatest} from "rxjs";
import {UserService} from "../../../../shared/services/user.service";
import {ClientViewModel} from "../../../../shared/models/client-view.model";
import {DetailPageComponent} from "../../../../shared/components/detail-page.component";
import {HelperService} from "../../../../shared/services/helper.service";
import {ClientInformationForm} from "../../forms/client-information-form";
import {ClientDto} from "../../../../shared/dtos/client-dto.model";
import {ClientArchiveActionForm} from "../../forms/client-archive-action-form";
import {ClientArchiveActionRequest} from "../../../../shared/dtos/client-archive-action-request.model";
import {ClientArchiveHistoryEntryReadDto} from "../../../../shared/dtos/client-archive-history-entry-read-dto.model";
import {ClientArchiveExportForm} from "../../forms/client-archive-export-form";
import {ClientArchiveExportRequest} from "../../../../shared/dtos/client-archive-export-request.model";
import {ClientArchiveExportStatusDto} from "../../../../shared/dtos/client-archive-export-status-dto.model";
import {ClientArchiveExportFormat} from "../../../../shared/dtos/client-archive-export-format.model";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmationModalComponent} from "../../../../shared/modals/confirmation-modal/confirmation-modal.component";
import {Converter} from "../../../../shared/services/converter.helper";
import {MatTabChangeEvent} from "@angular/material/tabs";
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
  MAT_NATIVE_DATE_FORMATS,
  NativeDateAdapter
} from "@angular/material/core";

@Component({
    selector: 'app-client-detail',
    templateUrl: './client-detail.component.html',
    styleUrls: ['./client-detail.component.css'],
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
export class ClientDetailComponent extends DetailPageComponent<ClientViewModel> implements OnInit {

  private static readonly archiveExportStepOrder = ['requested', 'generating', 'ready'] as const;

  // VARs
  institutions: InstitutionDto[] = [];
  categoryTemplates: CategoryTemplateDto[] = [];
  archiveHistory: ClientArchiveHistoryEntryReadDto[] = [];
  archiveExportStatus: ClientArchiveExportStatusDto | null = null;
  archiveExportFormats = [
    {value: ClientArchiveExportFormat.JSON, label: 'JSON'}
  ];
  archiveExportSteps = [
    {
      key: 'requested',
      title: 'Angefordert',
      description: 'Der Export wurde angefordert.'
    },
    {
      key: 'generating',
      title: 'Wird erstellt',
      description: 'Die JSON-Datei wird serverseitig erzeugt.'
    },
    {
      key: 'ready',
      title: 'Bereit zum Download',
      description: 'Der Downloadlink ist verfügbar.'
    }
  ] as const;

  // STATEs
  editMode = false;
  canManageArchive = false;
  selectedTabIndex = 0;
  isArchiveExportRequesting = false;
  isArchiveExportDownloading = false;

  infoForm = new ClientInformationForm();
  archiveActionForm = new ClientArchiveActionForm();
  archiveExportForm = new ClientArchiveExportForm();

  constructor(
    override helperService: HelperService,
    private institutionService: InstitutionService,
    private categoryTemplateService: CategoriesService,
    private clientService: ClientsService,
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private dialog: MatDialog,
    private converter: Converter) {
    super(helperService);
  }

  loadValues() {
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
          this.canManageArchive = user.access?.role === 1 || user.permissions
            .filter(perm => perm.changeInstitution)
            .some(perm => perm.institutionId === client.institution.id);
          this.value$.next(this.value);
          this.editValue = <ClientViewModel> {...this.value};
          this.institutions = institutions;
          this.categoryTemplates = categories;
          this.editMode = user.institutionId == this.value.dto.institution.id;

          this.refreshForm();
          this.loadArchiveState(id);
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

    if (this.value.dto.archived) {
      this.infoForm.disable({emitEvent: false});
    } else {
      this.infoForm.enable({emitEvent: false});
    }
  }

  getNewValue(): ClientViewModel {
    return new ClientViewModel();
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
    if (this.isSubmitting || this.value.dto.archived)
      return;

    this.isSubmitting = true;

    this.clientService.update(this.editValue.dto.id, this.editValue.dto).subscribe({
      next: () => this.handleSuccess("Klient geändert"),
      error: () => this.handleFailure("Fehler beim speichern")
    })
  }

  onTabChanged(event: MatTabChangeEvent) {
    this.selectedTabIndex = event.index;

    if (!this.canManageArchive) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {tab: null},
        queryParamsHandling: 'merge',
        replaceUrl: true
      }).then();
      return;
    }

    const selectedTab = this.getTabNameByIndex(event.index);
    if (selectedTab === 'archive') {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {tab: 'archive'},
        queryParamsHandling: 'merge',
        replaceUrl: true
      }).then();
      return;
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {tab: null},
      queryParamsHandling: 'merge',
      replaceUrl: true
    }).then();
  }

  get showArchiveTab(): boolean {
    return this.canManageArchive;
  }

  get showSaveButton(): boolean {
    return this.value.editable && !this.value.dto.archived;
  }

  get canEditGeneralFields(): boolean {
    return this.value.editable && !this.value.dto.archived;
  }

  get isArchived(): boolean {
    return this.value.dto.archived;
  }

  get archiveActionDescription(): string {
    return this.isArchived
      ? 'Wollen Sie den Klienten wirklich reaktivieren?'
      : 'Wollen Sie den Klienten wirklich archivieren?';
  }

  openArchiveConfirmation() {
    const dialogRef = this.dialog.open(ConfirmationModalComponent, {
      width: '520px'
    });

    dialogRef.componentInstance.title = this.isArchived ? 'Klient reaktivieren' : 'Klient archivieren';
    dialogRef.componentInstance.description = `${this.archiveActionDescription}<br><br>` +
      `Klient: <b>${this.value.dto.lastName} ${this.value.dto.firstName}</b><br>` +
      `Datum: <b>${this.formatArchiveActionDate(this.archiveActionForm.actionDate.value)}</b>`;

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.submitArchiveAction();
      }
    });
  }

  private submitArchiveAction() {
    if (this.isSubmitting || !this.archiveActionForm.valid) {
      return;
    }

    this.isSubmitting = true;

    const request = new ClientArchiveActionRequest();
    request.actionDate = this.archiveActionForm.actionDate.value;
    request.reason = this.archiveActionForm.reason.value;
    request.remark = this.archiveActionForm.remark.value;

    const action$ = this.isArchived
      ? this.clientService.reactivate(this.value.dto.id, request)
      : this.clientService.archive(this.value.dto.id, request);

    action$.subscribe({
      next: () => this.handleSuccess(this.isArchived ? 'Klient reaktiviert' : 'Klient archiviert'),
      error: () => this.handleFailure(this.isArchived ? 'Fehler beim reaktivieren' : 'Fehler beim archivieren')
    });
  }

  private loadArchiveState(clientId: string) {
    this.resetArchiveForm();
    this.resetArchiveExportState();

    if (!this.canManageArchive) {
      this.archiveHistory = [];
      this.archiveExportStatus = null;
      this.syncSelectedTab();
      return;
    }

    const historyRequest = this.clientService.getArchiveHistoryById(+clientId);
    const exportStatusRequest = this.clientService.getArchiveExportStatusById(+clientId);

    if (historyRequest == null || exportStatusRequest == null) {
      this.archiveHistory = [];
      this.archiveExportStatus = null;
      this.syncSelectedTab();
      return;
    }

    historyRequest.subscribe({
      next: (history) => {
        this.archiveHistory = history ?? [];
        this.syncSelectedTab();
      },
      error: () => {
        this.archiveHistory = [];
        this.syncSelectedTab();
      }
    });

    exportStatusRequest.subscribe({
      next: (status) => {
        this.archiveExportStatus = status ?? null;
      },
      error: () => {
        this.archiveExportStatus = null;
      }
    });
  }

  private resetArchiveForm() {
    this.archiveActionForm.actionDate.setValue(new Date(), {emitEvent: false});
    this.archiveActionForm.reason.setValue('', {emitEvent: false});
    this.archiveActionForm.remark.setValue('', {emitEvent: false});
    this.archiveActionForm.markAsPristine();
    this.archiveActionForm.markAsUntouched();
  }

  private resetArchiveExportState() {
    this.archiveExportForm.format.setValue(ClientArchiveExportFormat.JSON, {emitEvent: false});
    this.archiveExportForm.markAsPristine();
    this.archiveExportForm.markAsUntouched();
    this.isArchiveExportRequesting = false;
    this.isArchiveExportDownloading = false;
  }

  requestArchiveExport() {
    if (this.isArchiveExportRequesting || this.isArchiveExportDownloading || !this.archiveExportForm.valid) {
      return;
    }

    this.isArchiveExportRequesting = true;

    const request = new ClientArchiveExportRequest();
    request.format = this.archiveExportForm.format.value as ClientArchiveExportFormat;

    this.clientService.requestArchiveExport(this.value.dto.id, request).subscribe({
      next: status => this.handleArchiveExportStatus(status, 'Export angefordert'),
      error: () => this.handleArchiveExportRequestFailure('Fehler beim Anfordern des Exports')
    });
  }

  downloadArchiveExport() {
    const downloadLink = this.archiveExportStatus?.downloadLink;

    if (this.isArchiveExportRequesting || this.isArchiveExportDownloading || downloadLink == null) {
      return;
    }

    this.isArchiveExportDownloading = true;

    this.clientService.downloadArchiveExport(downloadLink).subscribe({
      next: () => {
        this.archiveExportStatus = null;
        this.isArchiveExportDownloading = false;
        this.helperService.openSnackBar('Export heruntergeladen');
      },
      error: () => this.handleArchiveExportDownloadFailure('Export nicht mehr verfügbar')
    });
  }

  private syncSelectedTab() {
    const requestedTab = (this.route.snapshot.queryParamMap.get('tab') ?? '').toLowerCase();

    if (requestedTab === 'archive') {
      if (this.canManageArchive) {
        this.selectedTabIndex = 2;
        return;
      }

      this.selectedTabIndex = 0;
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {tab: null},
        queryParamsHandling: 'merge',
        replaceUrl: true
      }).then();
      return;
    }

    this.selectedTabIndex = requestedTab === 'plans' ? 1 : 0;
  }

  private getTabNameByIndex(index: number): string {
    if (index === 0) {
      return 'general';
    }

    if (index === 1) {
      return 'plans';
    }

    return 'archive';
  }

  formatArchiveActionType(actionType: string): string {
    if (actionType === 'REACTIVATE') {
      return 'Reaktivierung';
    }

    return 'Archivierung';
  }

  formatArchiveActionDate(actionDate: string | Date | null): string {
    if (!actionDate) {
      return '';
    }

    const date = actionDate instanceof Date ? actionDate : new Date(actionDate);
    return this.converter.formatDateToGerman(date);
  }

  formatArchiveActionTimestamp(actionTimestamp: string | Date | null): string {
    if (!actionTimestamp) {
      return '';
    }

    const timestamp = actionTimestamp instanceof Date ? actionTimestamp : new Date(actionTimestamp);
    return this.converter.formatDateToGermanTime(timestamp);
  }

  formatArchiveEmployee(entry: ClientArchiveHistoryEntryReadDto): string {
    return `${entry.executingEmployeeFirstname} ${entry.executingEmployeeLastname} (#${entry.executingEmployeeId})`;
  }

  get hasArchiveExportDownloadLink(): boolean {
    return this.archiveExportStatus?.downloadLink != null;
  }

  get archiveExportStage(): 'requested' | 'generating' | 'ready' {
    if (this.isArchiveExportRequesting) {
      return 'generating';
    }

    if (this.hasArchiveExportDownloadLink) {
      return 'ready';
    }

    return 'requested';
  }

  get archiveExportDownloadExpiresAt(): string {
    return this.formatArchiveActionTimestamp(this.archiveExportStatus?.downloadLink?.downloadLinkExpiresAt ?? null);
  }

  isArchiveExportStepCompleted(step: 'requested' | 'generating' | 'ready'): boolean {
    return ClientDetailComponent.archiveExportStepOrder.indexOf(step) <
      ClientDetailComponent.archiveExportStepOrder.indexOf(this.archiveExportStage);
  }

  isArchiveExportStepCurrent(step: 'requested' | 'generating' | 'ready'): boolean {
    return this.archiveExportStage === step;
  }

  private handleArchiveExportStatus(status: ClientArchiveExportStatusDto, successMessage: string) {
    this.archiveExportStatus = status;
    this.isArchiveExportRequesting = false;
    this.isArchiveExportDownloading = false;
    this.helperService.openSnackBar(successMessage);
  }

  private handleArchiveExportRequestFailure(message: string) {
    this.isArchiveExportRequesting = false;
    this.isArchiveExportDownloading = false;
    this.helperService.openSnackBar(message);
  }

  private handleArchiveExportDownloadFailure(message: string) {
    this.archiveExportStatus = null;
    this.isArchiveExportRequesting = false;
    this.isArchiveExportDownloading = false;
    this.helperService.openSnackBar(message);
  }
}
