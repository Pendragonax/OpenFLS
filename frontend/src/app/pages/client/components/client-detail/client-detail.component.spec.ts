import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {convertToParamMap, ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {DateAdapter} from '@angular/material/core';
import {Subject, of} from 'rxjs';
import {vi} from 'vitest';

import {ClientDetailComponent} from './client-detail.component';
import {InformationRowComponent} from '../../../../shared/components/information-row/information-row.component';
import {ClientsService} from '../../../../shared/services/clients.service';
import {InstitutionService} from '../../../../shared/services/institution.service';
import {CategoriesService} from '../../../../shared/services/categories.service';
import {UserService} from '../../../../shared/services/user.service';
import {HelperService} from '../../../../shared/services/helper.service';
import {ClientDto} from '../../../../shared/dtos/client-dto.model';
import {InstitutionDto} from '../../../../shared/dtos/institution-dto.model';
import {CategoryTemplateDto} from '../../../../shared/dtos/category-template-dto.model';
import {EmployeeDto} from '../../../../shared/dtos/employee-dto.model';
import {ClientArchiveHistoryEntryReadDto as ArchiveHistoryDto} from '../../../../shared/dtos/client-archive-history-entry-read-dto.model';
import {ClientArchiveExportStatusDto} from '../../../../shared/dtos/client-archive-export-status-dto.model';
import {ClientArchiveExportFormat} from '../../../../shared/dtos/client-archive-export-format.model';

class MockClientsService {
  getById = vi.fn();
  getArchiveHistoryById = vi.fn();
  getArchiveExportStatusById = vi.fn();
  archive = vi.fn();
  reactivate = vi.fn();
  requestArchiveExport = vi.fn();
  downloadArchiveExport = vi.fn();
  update = vi.fn();
}

class MockHelperService {
  openSnackBar = vi.fn();
}

function createClient(archived = false): ClientDto {
  const client = new ClientDto();
  client.id = 1;
  client.firstName = 'Anna';
  client.lastName = 'Beispiel';
  client.phoneNumber = '0123';
  client.email = 'anna@example.org';
  client.archived = archived;
  client.institution = Object.assign(new InstitutionDto(), {id: 1, name: 'Bereich A'});
  client.categoryTemplate = Object.assign(new CategoryTemplateDto(), {id: 3, title: 'Vorlage A'});
  return client;
}

function createUser(isAdmin: boolean, canLeadInstitution: boolean): EmployeeDto {
  const user = new EmployeeDto();
  user.id = 10;
  user.firstName = 'Lea';
  user.lastName = 'Ding';
  user.institutionId = 1;
  user.access!.role = isAdmin ? 1 : 3;
  user.permissions = [{
    employeeId: 10,
    institutionId: 1,
    writeEntries: true,
    readEntries: true,
    changeInstitution: canLeadInstitution,
    affiliated: true
  }];
  return user;
}

describe('ClientDetailComponent', () => {
  let component: ClientDetailComponent;
  let fixture: ComponentFixture<ClientDetailComponent>;

  let currentClient = createClient(false);
  let currentHistory: ClientArchiveHistoryEntryReadDto[] = [];
  let currentExportStatus: ClientArchiveExportStatusDto | null = null;
  let currentUser = createUser(true, true);
  let currentTab: string | null = null;
  const clientsService = new MockClientsService();
  const helperService = new MockHelperService();
  const routerNavigate = vi.fn(() => Promise.resolve(true));
  const dialogOpen = vi.fn(() => ({
    componentInstance: {},
    afterClosed: () => of(true)
  }));

  const route = {
    snapshot: {
      paramMap: convertToParamMap({id: '1'}),
      queryParamMap: convertToParamMap({})
    }
  } as unknown as ActivatedRoute;

  const router = {
    navigate: routerNavigate
  } as unknown as Router;

  const dialog = {
    open: dialogOpen
  } as unknown as MatDialog;

  const institutionService = {
    get allValues$() {
      return of([Object.assign(new InstitutionDto(), {id: 1, name: 'Bereich A'})]);
    }
  } as unknown as InstitutionService;

  const categoriesService = {
    get allValues$() {
      return of([Object.assign(new CategoryTemplateDto(), {id: 3, title: 'Vorlage A'})]);
    }
  } as unknown as CategoriesService;

  const userService = {
    get user$() {
      return of(currentUser);
    }
  } as unknown as UserService;

  beforeEach(async () => {
    routerNavigate.mockReset();
    dialogOpen.mockReset();
    clientsService.getById.mockReset();
    clientsService.getArchiveHistoryById.mockReset();
    clientsService.getArchiveExportStatusById.mockReset();
    clientsService.archive.mockReset();
    clientsService.reactivate.mockReset();
    clientsService.requestArchiveExport.mockReset();
    clientsService.downloadArchiveExport.mockReset();
    clientsService.update.mockReset();

    await TestBed.configureTestingModule({
      declarations: [ClientDetailComponent, InformationRowComponent],
      providers: [
        {provide: ActivatedRoute, useValue: route},
        {provide: Router, useValue: router},
        {provide: MatDialog, useValue: dialog},
        {provide: ClientsService, useValue: clientsService},
        {provide: InstitutionService, useValue: institutionService},
        {provide: CategoriesService, useValue: categoriesService},
        {provide: UserService, useValue: userService},
        {provide: HelperService, useValue: helperService}
      ]
    }).compileComponents();
  });

  function configureScenario(options?: {
    archived?: boolean;
    isAdmin?: boolean;
    canLeadInstitution?: boolean;
    tab?: string | null;
    history?: ClientArchiveHistoryEntryReadDto[];
    exportStatus?: ClientArchiveExportStatusDto | null;
  }) {
    currentClient = createClient(options?.archived ?? false);
    currentHistory = options?.history ?? [];
    currentExportStatus = options?.exportStatus ?? null;
    currentUser = createUser(options?.isAdmin ?? true, options?.canLeadInstitution ?? true);
    currentTab = options?.tab ?? null;

    (route as {snapshot: {queryParamMap: ReturnType<typeof convertToParamMap>}}).snapshot.queryParamMap = convertToParamMap(currentTab ? {tab: currentTab} : {});
    clientsService.getById.mockImplementation(() => of(currentClient));
    clientsService.getArchiveHistoryById.mockImplementation(() => of(currentHistory));
    clientsService.getArchiveExportStatusById.mockImplementation(() => of(currentExportStatus as ClientArchiveExportStatusDto));
    clientsService.archive.mockImplementation(() => of(currentHistory[0] ?? {
      id: 99,
      actionType: 'ARCHIVE',
      actionDate: '2026-05-23',
      actionTimestamp: '2026-05-23T10:15:00',
      reason: 'Grund',
      remark: 'Bemerkung',
      executingEmployeeId: 10,
      executingEmployeeFirstname: 'Lea',
      executingEmployeeLastname: 'Ding'
    }));
    clientsService.reactivate.mockImplementation(() => of(currentHistory[0] ?? {
      id: 100,
      actionType: 'REACTIVATE',
      actionDate: '2026-05-23',
      actionTimestamp: '2026-05-23T10:15:00',
      reason: 'Grund',
      remark: 'Bemerkung',
      executingEmployeeId: 10,
      executingEmployeeFirstname: 'Lea',
      executingEmployeeLastname: 'Ding'
    }));
    clientsService.requestArchiveExport.mockImplementation(() => of(currentExportStatus ?? {
      ready: true,
      format: ClientArchiveExportFormat.JSON,
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 10,
      downloadLink: {
        downloadLink: '/clients/1/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    }));
    clientsService.downloadArchiveExport.mockImplementation(() => of(void 0));

    fixture = TestBed.createComponent(ClientDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    configureScenario();

    expect(component).toBeTruthy();
  });

  it('should redirect archive tab access for normal employees', () => {
    configureScenario({tab: 'archive', isAdmin: false, canLeadInstitution: false});

    expect(routerNavigate).toHaveBeenCalled();
    expect(component.selectedTabIndex).toBe(0);
    expect(component.showArchiveTab).toBe(false);
  });

  it('should render archive controls and history for privileged users', () => {
    const historyEntry: ArchiveHistoryDto = {
      id: 7,
      actionType: 'ARCHIVE',
      actionDate: '2026-05-23',
      actionTimestamp: '2026-05-23T10:15:00',
      reason: 'Umzug',
      remark: 'Extern verwaltet',
      executingEmployeeId: 42,
      executingEmployeeFirstname: 'Maria',
      executingEmployeeLastname: 'Muster'
    };

    configureScenario({tab: 'archive', history: [historyEntry]});

    const text = fixture.nativeElement.textContent as string;

    expect(component.showArchiveTab).toBe(true);
    expect(component.selectedTabIndex).toBe(2);
    expect(fixture.nativeElement.querySelector('.archive-state-banner')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-columns')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-panel--form')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-panel--export')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-panel--history')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-panel--export .archive-export-stepper')).toBeTruthy();
    expect(fixture.nativeElement.querySelectorAll('.archive-export-step').length).toBe(3);
    expect(fixture.nativeElement.querySelector('.archive-history-scroll')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-history-entry--archive')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-history-entry__details')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-history-entry__details-column--secondary')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.archive-history-entry__details-column .col-sm-4')).toBeTruthy();
    expect(text).toContain('Status');
    expect(text).toContain('Aktiv');
    expect(text).toContain('Archivieren');
    expect(text).toContain('Daten exportieren');
    expect(text).toContain('Angefordert');
    expect(text).toContain('Wird erstellt');
    expect(text).toContain('Bereit zum Download');
    expect(text).toContain('Export anfordern');
    expect(text).toContain('Archivierung');
    expect(text).toContain('23.05.2026');
    expect(text).toContain('Ausgeführt am');
    expect(text).toContain('23.05.2026 10:15');
    expect(text).toContain('Maria Muster (#42)');
    expect(text).toContain('Extern verwaltet');
  });

  it('should show a download button for an already prepared export', () => {
    const exportStatus: ClientArchiveExportStatusDto = {
      ready: true,
      format: ClientArchiveExportFormat.JSON,
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 10,
      downloadLink: {
        downloadLink: '/clients/1/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    };

    configureScenario({tab: 'archive', exportStatus});

    const text = fixture.nativeElement.textContent as string;

    expect(component.hasArchiveExportDownloadLink).toBe(true);
    expect(text).toContain('Download (JSON)');
    expect(text).toContain('Link gültig bis');
  });

  it('should show a loading state while the export is being requested', () => {
    configureScenario({tab: 'archive'});

    const exportRequest$ = new Subject<ClientArchiveExportStatusDto>();
    clientsService.requestArchiveExport.mockReturnValue(exportRequest$.asObservable());
    component.requestArchiveExport();
    fixture.detectChanges();

    expect(component.isArchiveExportRequesting).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Export wird erstellt');

    exportRequest$.next({
      ready: true,
      format: ClientArchiveExportFormat.JSON,
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 10,
      downloadLink: {
        downloadLink: '/clients/1/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    });
    exportRequest$.complete();
    fixture.detectChanges();

    expect(component.isArchiveExportRequesting).toBe(false);
    expect(component.hasArchiveExportDownloadLink).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Download (JSON)');
  });

  it('should remove the export link after a successful download', () => {
    const exportStatus: ClientArchiveExportStatusDto = {
      ready: true,
      format: ClientArchiveExportFormat.JSON,
      requestedAt: '2026-06-13T10:15:00',
      requestedByEmployeeId: 10,
      downloadLink: {
        downloadLink: '/clients/1/archive/export/token-123',
        downloadLinkExpiresAt: '2026-06-13T10:35:00',
        downloadedAt: null
      }
    };

    configureScenario({tab: 'archive', exportStatus});
    clientsService.downloadArchiveExport.mockReturnValue(of(void 0));

    component.downloadArchiveExport();
    fixture.detectChanges();

    expect(clientsService.downloadArchiveExport).toHaveBeenCalled();
    expect(component.hasArchiveExportDownloadLink).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Export anfordern');
  });

  it('should format archive date input like other german date pickers', () => {
    configureScenario({tab: 'archive'});

    const dateAdapter = fixture.debugElement.injector.get(DateAdapter);
    const formatted = dateAdapter.format(
      new Date('2026-05-23T00:00:00'),
      {year: 'numeric', month: 'numeric', day: 'numeric'}
    );

    expect(formatted).toMatch(/23\.(5|05)\.2026/);
    expect(formatted).not.toContain('/');
  });

  it('should hide save button and disable general fields for archived clients', () => {
    configureScenario({archived: true});

    const text = fixture.nativeElement.textContent as string;
    const firstNameInput = fixture.nativeElement.querySelector('input[formcontrolname="firstName"]') as HTMLInputElement;

    expect(component.isArchived).toBe(true);
    expect(component.showSaveButton).toBe(false);
    expect(text).not.toContain('Speichern');
    expect(firstNameInput.disabled).toBe(true);
    expect(text).toContain('Archiviert');
  });

  it('should submit archive action after confirmation', () => {
    configureScenario();
    component.archiveActionForm.reason.setValue('Testgrund');
    component.archiveActionForm.remark.setValue('Testbemerkung');
    component.archiveActionForm.actionDate.setValue(new Date('2026-05-23T00:00:00'));

    component.openArchiveConfirmation();

    expect(dialogOpen).toHaveBeenCalled();
    expect(clientsService.archive).toHaveBeenCalled();
    expect(clientsService.reactivate).not.toHaveBeenCalled();
  });
});
