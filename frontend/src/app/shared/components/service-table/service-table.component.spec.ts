import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ServiceTableComponent } from './service-table.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { UserService } from '../../services/user.service';
import { ServiceService } from '../../services/service.service';
import { CsvService } from '../../services/csv.service';

describe('ServiceViewTableComponent', () => {
  let component: ServiceTableComponent;
  let fixture: ComponentFixture<ServiceTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceTableComponent],
      providers: [
        { provide: NgbModal, useValue: { open: () => ({ result: Promise.resolve(false) }) } },
        { provide: UserService, useValue: { isAdmin$: of(true) } },
        { provide: ServiceService, useValue: { delete: () => of({}) } },
        { provide: CsvService, useValue: { exportToCsvWithHeader: () => null } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServiceTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not include minutes column in edit mode', () => {
    component.editMode = true;
    component.services = [{
      id: 1,
      start: '2026-02-15T08:00:00',
      end: '2026-02-15T09:30:00',
      minutes: 90,
      title: '',
      content: '',
      groupOffer: false,
      institution: { id: 1, name: 'Inst', email: '', phonenumber: '' } as any,
      employee: { id: 1, firstname: 'Max', lastname: 'M' } as any,
      client: { id: 1, firstName: 'Anna', lastName: 'A' } as any
    }] as any;

    component.ngOnChanges({
      services: {
        currentValue: component.services,
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false
      }
    });

    expect(component.displayedColumns).toEqual(['start', 'content', 'clientFullName', 'actions']);
  });

  it('should render start cell with end time and minute pill', () => {
    const html = component.transformDateRangeString(
      '2026-02-15T08:00:00',
      '2026-02-15T09:30:00',
      90
    );

    expect(html).toContain('08:00 - 09:30');
    expect(html).toContain('90 Min');
    expect(html).toContain('service-time-pill');
  });
});
