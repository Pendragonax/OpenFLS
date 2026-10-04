import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {of} from 'rxjs';
import {vi} from 'vitest';
import {ClientComponent} from './client.component';
import {ClientsService} from '../../shared/services/clients.service';
import {UserService} from '../../shared/services/user.service';
import {InstitutionService} from '../../shared/services/institution.service';
import {ServiceService} from '../../shared/services/service.service';
import {HelperService} from '../../shared/services/helper.service';
import {ClientDashboardService} from '../../shared/services/client-dashboard.service';
import {Comparer} from '../../shared/services/comparer.helper';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EmployeeDto} from '../../shared/dtos/employee-dto.model';

class MockClientsService {
  getAll = vi.fn();
}

let currentUser = createUser(true);

class MockUserService {
  get user$() {
    return of(currentUser);
  }
}

class MockInstitutionService {
  getAllReadable = vi.fn().mockReturnValue(of([{id: 1, name: 'Bereich A'}]));
}

class MockServiceService {
  getCountByClientId = vi.fn().mockReturnValue(of(0));
}

class MockHelperService {
  openSnackBar = vi.fn();
}

class MockClientDashboardService {
  getFavorites = vi.fn().mockReturnValue(of([]));
  addFavorite = vi.fn().mockReturnValue(of(undefined));
  deleteFavorite = vi.fn().mockReturnValue(of(undefined));
}

function createUser(isAdmin: boolean, canLead = false): EmployeeDto {
  const user = new EmployeeDto();
  user.id = 10;
  user.access!.role = isAdmin ? 1 : 3;
  user.permissions = [
    {
      employeeId: 10,
      institutionId: 1,
      writeEntries: true,
      readEntries: true,
      changeInstitution: canLead,
      affiliated: true
    }
  ];
  return user;
}

function createClient(id: number, archived: boolean) {
  return {
    id,
    firstName: archived ? 'Archiv' : 'Aktiv',
    lastName: 'Kunde',
    phoneNumber: '0123',
    email: 'test@example.org',
    archived,
    institution: {id: 1, name: 'Bereich A'}
  };
}

describe('ClientComponent', () => {
  let component: ClientComponent;
  let fixture: ComponentFixture<ClientComponent>;
  let clientsService: MockClientsService;

  beforeEach(async () => {
    clientsService = new MockClientsService();
    clientsService.getAll.mockReturnValue(of([
      createClient(1, false),
      createClient(2, true)
    ]));

    await TestBed.configureTestingModule({
      declarations: [ClientComponent],
      providers: [
        {provide: ClientsService, useValue: clientsService},
        {provide: UserService, useClass: MockUserService},
        {provide: InstitutionService, useClass: MockInstitutionService},
        {provide: ServiceService, useClass: MockServiceService},
        {provide: HelperService, useClass: MockHelperService},
        {provide: ClientDashboardService, useClass: MockClientDashboardService},
        {provide: MatDialog, useValue: {open: vi.fn()}},
        {provide: Comparer, useValue: {compare: (a: any, b: any, isAsc: boolean) => (a < b ? -1 : a > b ? 1 : 0) * (isAsc ? 1 : -1)}},
        {provide: NgbModal, useValue: {open: vi.fn()}}
      ]
    }).compileComponents();
  });

  it('hides archived clients by default', () => {
    currentUser = createUser(false, false);
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.values.map(value => value.dto.id)).toEqual([1]);
    expect(component.tableSource.data.map(value => value.dto.id)).toEqual([1]);
  });

  it('reveals archived clients when the toggle is enabled', () => {
    currentUser = createUser(true);
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.onArchivedVisibilityChanged(true);

    expect(component.values.map(value => value.dto.id)).toEqual([1, 2]);
    expect(component.tableSource.data.map(value => value.dto.id)).toEqual([1, 2]);
  });

  it('marks rows already favourited by the signed in employee', () => {
    currentUser = createUser(false, false);
    const clientDashboardService = TestBed.inject(ClientDashboardService) as unknown as MockClientDashboardService;
    clientDashboardService.getFavorites.mockReturnValue(of([{clientId: 1}]));
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.values.find(value => value.dto.id === 1)?.favorite).toBe(true);
  });

  it('adds a client to the favourites without asking for confirmation', () => {
    currentUser = createUser(false, false);
    const clientDashboardService = TestBed.inject(ClientDashboardService) as unknown as MockClientDashboardService;
    const matDialog = TestBed.inject(MatDialog) as unknown as {open: ReturnType<typeof vi.fn>};
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    const client = component.values[0];
    const event = {stopPropagation: vi.fn(), preventDefault: vi.fn()} as unknown as Event;

    component.toggleFavorite(client, event);

    expect(matDialog.open).not.toHaveBeenCalled();
    expect(clientDashboardService.addFavorite).toHaveBeenCalledWith(client.dto.id);
    expect(client.favorite).toBe(true);
  });

  it('asks for confirmation before removing a client from the favourites', () => {
    currentUser = createUser(false, false);
    const clientDashboardService = TestBed.inject(ClientDashboardService) as unknown as MockClientDashboardService;
    clientDashboardService.getFavorites.mockReturnValue(of([{clientId: 1}]));
    const matDialog = TestBed.inject(MatDialog) as unknown as {open: ReturnType<typeof vi.fn>};
    matDialog.open.mockReturnValue({componentInstance: {}, afterClosed: () => of(true)});
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    const client = component.values.find(value => value.dto.id === 1)!;
    const event = {stopPropagation: vi.fn(), preventDefault: vi.fn()} as unknown as Event;

    component.toggleFavorite(client, event);

    expect(matDialog.open).toHaveBeenCalled();
    expect(clientDashboardService.deleteFavorite).toHaveBeenCalledWith(1);
    expect(client.favorite).toBe(false);
  });
});
