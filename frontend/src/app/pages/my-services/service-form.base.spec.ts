import '@testbed';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ServiceFormBase } from './service-form.base';
import { UserService } from '../../shared/services/user.service';
import { InstitutionService } from '../../shared/services/institution.service';
import { ClientsService } from '../../shared/services/clients.service';
import { HourTypeService } from '../../shared/services/hour-type.service';
import { CategoriesService } from '../../shared/services/categories.service';
import { SponsorService } from '../../shared/services/sponsor.service';
import { ServiceService } from '../../shared/services/service.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Converter } from '../../shared/services/converter.helper';
import { HelperService } from '../../shared/services/helper.service';
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
  update() {
    return of({});
  }
  create() {
    return of({});
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

@Component({
  selector: 'app-test-service-form',
  template: '',
  standalone: false
})
class TestServiceFormComponent extends ServiceFormBase {
  override ngOnInit() {}

  callSetupValueSync() {
    this.setupValueSyncSubscriptions();
  }

  callGetSelectableAssistancePlansForEdit(plans: any[], serviceDate: string, currentPlanId: number | null) {
    return this.getSelectableAssistancePlansForEdit(plans as any, serviceDate, currentPlanId);
  }
}

describe('ServiceFormBase', () => {
  let component: TestServiceFormComponent;
  let fixture: ComponentFixture<TestServiceFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestServiceFormComponent],
      providers: [
        { provide: UserService, useClass: MockUserService },
        { provide: InstitutionService, useClass: MockInstitutionService },
        { provide: ClientsService, useClass: MockClientsService },
        { provide: HourTypeService, useClass: MockHourTypeService },
        { provide: CategoriesService, useClass: MockCategoriesService },
        { provide: SponsorService, useClass: MockSponsorService },
        { provide: ServiceService, useClass: MockServiceService },
        { provide: ActivatedRoute, useClass: MockActivatedRoute },
        { provide: Router, useClass: MockRouter },
        { provide: Converter, useClass: MockConverter },
        { provide: HelperService, useClass: MockHelperService },
        { provide: Location, useClass: MockLocation }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TestServiceFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should sync hourType and institution into value', () => {
    component.callSetupValueSync();
    component.hourTypeControl.setValue(5);
    component.institutionControl.setValue(2);
    expect(component.value.hourTypeId).toBe(5);
    expect(component.value.institutionId).toBe(2);
  });

  it('should only return date-selectable assistance plans in edit if current plan is selectable', () => {
    const plans = [
      {id: 1, start: '2026-01-01', end: '2026-01-31'},
      {id: 2, start: '2026-02-01', end: '2026-02-28'}
    ];

    const result = component.callGetSelectableAssistancePlansForEdit(plans, '2026-01-15', 1);

    expect(result.map(plan => plan.id)).toEqual([1]);
  });

  it('should keep current assistance plan in edit even if outside service date', () => {
    const plans = [
      {id: 1, start: '2026-01-01', end: '2026-01-31'},
      {id: 2, start: '2026-02-01', end: '2026-02-28'}
    ];

    const result = component.callGetSelectableAssistancePlansForEdit(plans, '2026-01-15', 2);

    expect(result.map(plan => plan.id)).toEqual([2, 1]);
  });

  it('should return institution name for assistance plan label', () => {
    const institutionName = component.getInstitutionName({
      id: 1,
      start: '2026-01-01',
      end: '2026-01-31',
      clientId: 1,
      institutionId: 2,
      institutionName: 'Bereich Nord',
      sponsorId: 3,
      hours: [],
      goals: []
    } as any);

    expect(institutionName).toBe('Bereich Nord');
  });
});
