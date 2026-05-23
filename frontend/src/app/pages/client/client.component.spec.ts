import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {vi} from 'vitest';
import {ClientComponent} from './client.component';
import {ClientsService} from '../../shared/services/clients.service';
import {UserService} from '../../shared/services/user.service';
import {InstitutionService} from '../../shared/services/institution.service';
import {ServiceService} from '../../shared/services/service.service';
import {HelperService} from '../../shared/services/helper.service';
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
    lastName: archived ? 'Kunde' : 'Kunde',
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
        {provide: Comparer, useValue: {compare: (a: any, b: any, isAsc: boolean) => (a < b ? -1 : a > b ? 1 : 0) * (isAsc ? 1 : -1)}},
        {provide: NgbModal, useValue: {open: vi.fn()}}
      ]
    }).compileComponents();
  });

  it('should render the clients returned by the backend', () => {
    currentUser = createUser(false, false);
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.values.map(value => value.dto.id)).toEqual([1, 2]);
    expect(component.tableSource.data.map(value => value.dto.id)).toEqual([1, 2]);
  });

  it('should keep archived entries in the data set when the backend returns them', () => {
    currentUser = createUser(true);
    fixture = TestBed.createComponent(ClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.values[1].dto.archived).toBe(true);
    expect(component.tableSource.data[1].dto.archived).toBe(true);
  });
});
