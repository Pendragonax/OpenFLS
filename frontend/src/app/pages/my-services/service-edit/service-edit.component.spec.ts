import '@testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';
import { ServiceEditComponent } from './service-edit.component';
import { UserService } from '../../../shared/services/user.service';
import { InstitutionService } from '../../../shared/services/institution.service';
import { ClientsService } from '../../../shared/services/clients.service';
import { HourTypeService } from '../../../shared/services/hour-type.service';
import { CategoriesService } from '../../../shared/services/categories.service';
import { SponsorService } from '../../../shared/services/sponsor.service';
import { ServiceService } from '../../../shared/services/service.service';
import { AssistancePlanService } from '../../../shared/services/assistance-plan.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Converter } from '../../../shared/services/converter.helper';
import { HelperService } from '../../../shared/services/helper.service';
import { Location } from '@angular/common';

class MockUserService {
  writeableInstitutions$ = of([1]);
  user$ = of({ id: 1 });
}

class MockInstitutionService {
  allValues$ = of([{ id: 1, name: 'Inst' }]);
}

class MockClientsService {
  allValues$ = of([{ id: 1, firstName: 'Max', lastName: 'M' }]);
  getById() {
    return of({
      id: 1,
      assistancePlans: [],
      categoryTemplate: { categories: [] }
    });
  }
}

class MockHourTypeService {
  allValues$ = of([]);
}

class MockCategoriesService {}

class MockSponsorService {
  allValues$ = of([]);
}

class MockServiceService {
  getClientAndDateServices() {
    return of({ services: [] });
  }
  getByAssistancePlan() {
    return of([]);
  }
  create() {
    return of({});
  }
  update() {
    return of({});
  }
  getById() {
    return of({
      id: 1,
      start: Date.now().toString(),
      end: Date.now().toString(),
      title: '',
      content: '',
      minutes: 0,
      unfinished: false,
      groupService: false,
      employeeId: 1,
      clientId: 1,
      institutionId: 1,
      assistancePlanId: 0,
      hourTypeId: 0,
      goals: [],
      categorys: []
    });
  }
}

class MockAssistancePlanService {
  getEvaluationLeftById() {
    return of({ hourTypeEvaluation: [] });
  }
}

class MockActivatedRoute {
  snapshot = {
    paramMap: {
      get: () => null
    }
  };
}

class MockRouter {}

class MockConverter {
  formatDate(date: Date) {
    return date.toISOString();
  }
  formatDateToGerman(date: Date) {
    return date.toISOString();
  }
  getDateTimeString(date: string, hour: number, minute: number) {
    return `${date}T${hour}:${minute}`;
  }
}

class MockHelperService {
  openSnackBar() {}
}

class MockLocation {
  back() {}
}

describe('ServiceEditComponent', () => {
  let component: ServiceEditComponent;
  let fixture: ComponentFixture<ServiceEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceEditComponent],
      providers: [
        { provide: UserService, useClass: MockUserService },
        { provide: InstitutionService, useClass: MockInstitutionService },
        { provide: ClientsService, useClass: MockClientsService },
        { provide: HourTypeService, useClass: MockHourTypeService },
        { provide: CategoriesService, useClass: MockCategoriesService },
        { provide: SponsorService, useClass: MockSponsorService },
        { provide: ServiceService, useClass: MockServiceService },
        { provide: AssistancePlanService, useClass: MockAssistancePlanService },
        { provide: ActivatedRoute, useClass: MockActivatedRoute },
        { provide: Router, useClass: MockRouter },
        { provide: Converter, useClass: MockConverter },
        { provide: HelperService, useClass: MockHelperService },
        { provide: Location, useClass: MockLocation }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .overrideComponent(ServiceEditComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(ServiceEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
