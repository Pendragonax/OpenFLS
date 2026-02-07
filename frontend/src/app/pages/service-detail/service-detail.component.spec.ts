import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { BehaviorSubject, of } from 'rxjs';
import { ServiceDetailComponent } from './service-detail.component';
import { UserService } from '../../shared/services/user.service';
import { InstitutionService } from '../../shared/services/institution.service';
import { ClientsService } from '../../shared/services/clients.service';
import { HourTypeService } from '../../shared/services/hour-type.service';
import { CategoriesService } from '../../shared/services/categories.service';
import { SponsorService } from '../../shared/services/sponsor.service';
import { ServiceService } from '../../shared/services/service.service';
import { ActivatedRoute } from '@angular/router';
import { HelperService } from '../../shared/services/helper.service';
import { Location } from '@angular/common';
import { Converter } from '../../shared/services/converter.helper';
import { EmployeeDto } from '../../shared/dtos/employee-dto.model';
import { InstitutionDto } from '../../shared/dtos/institution-dto.model';
import { ClientDto } from '../../shared/dtos/client-dto.model';
import { HourTypeDto } from '../../shared/dtos/hour-type-dto.model';
import { SponsorDto } from '../../shared/dtos/sponsor-dto.model';
import { convertToParamMap } from '@angular/router';

describe('ServiceNewComponent', () => {
  let component: ServiceDetailComponent;
  let fixture: ComponentFixture<ServiceDetailComponent>;
  let writeableInstitutions$: BehaviorSubject<number[]>;
  let institutions$: BehaviorSubject<InstitutionDto[]>;
  let clients$: BehaviorSubject<ClientDto[]>;
  let hourTypes$: BehaviorSubject<HourTypeDto[]>;
  let user$: BehaviorSubject<EmployeeDto>;
  let sponsors$: BehaviorSubject<SponsorDto[]>;

  beforeEach(async () => {
    writeableInstitutions$ = new BehaviorSubject<number[]>([]);
    institutions$ = new BehaviorSubject<InstitutionDto[]>([]);
    clients$ = new BehaviorSubject<ClientDto[]>([]);
    hourTypes$ = new BehaviorSubject<HourTypeDto[]>([]);
    user$ = new BehaviorSubject<EmployeeDto>(new EmployeeDto());
    sponsors$ = new BehaviorSubject<SponsorDto[]>([]);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ ServiceDetailComponent ],
      providers: [
        {
          provide: UserService,
          useValue: {
            writeableInstitutions$,
            user$,
          },
        },
        { provide: InstitutionService, useValue: { allValues$: institutions$ } },
        { provide: ClientsService, useValue: { allValues$: clients$, getById: () => of(new ClientDto()) } },
        { provide: HourTypeService, useValue: { allValues$: hourTypes$ } },
        { provide: CategoriesService, useValue: { allValues$: of([]) } },
        { provide: SponsorService, useValue: { allValues$: sponsors$ } },
        { provide: ServiceService, useValue: { getById: () => of() } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
            },
          },
        },
        { provide: HelperService, useValue: { openSnackBar: () => {} } },
        { provide: Location, useValue: { back: () => {}, go: () => {} } },
        { provide: Converter, useValue: { formatDate: () => '', getDateTimeString: () => '', formatDateToGerman: () => '' } },
      ],
    })
    .overrideComponent(ServiceDetailComponent, { set: { template: '' } })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('unsubscribe_afterDestroy_doesNotUpdateState', () => {
    // Given
    fixture.detectChanges();
    component.titleControl.setValue('Title A');
    expect(component.value.title).toBe('Title A');

    // When
    fixture.destroy();
    component.titleControl.setValue('Title B');

    // Then
    expect(component.value.title).toBe('Title A');
  });
});
