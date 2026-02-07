import {Component} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from "@angular/material/core";
import {ServiceDetailComponent} from "../../service-detail/service-detail.component";
import {UserService} from "../../../shared/services/user.service";
import {InstitutionService} from "../../../shared/services/institution.service";
import {ClientsService} from "../../../shared/services/clients.service";
import {HourTypeService} from "../../../shared/services/hour-type.service";
import {CategoriesService} from "../../../shared/services/categories.service";
import {SponsorService} from "../../../shared/services/sponsor.service";
import {ServiceService} from "../../../shared/services/service.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Converter} from "../../../shared/services/converter.helper";
import {HelperService} from "../../../shared/services/helper.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-service-beta-new',
  templateUrl: './service-beta-new.component.html',
  styleUrls: ['./service-beta-new.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS },
  ],
  standalone: false
})
export class ServiceBetaNewComponent extends ServiceDetailComponent {
  constructor(
    userService: UserService,
    institutionService: InstitutionService,
    clientService: ClientsService,
    hourTypeService: HourTypeService,
    categoryService: CategoriesService,
    sponsorService: SponsorService,
    serviceService: ServiceService,
    router: Router,
    route: ActivatedRoute,
    converter: Converter,
    helperService: HelperService,
    location: Location
  ) {
    super(
      userService,
      institutionService,
      clientService,
      hourTypeService,
      categoryService,
      sponsorService,
      serviceService,
      router,
      route,
      converter,
      helperService,
      location
    );
  }

  resetAll() {
    this.firstForm.reset({
      serviceDate: this.timeNow,
      startHour: this.timeNow.getHours(),
      startMinute: this.timeNow.getMinutes(),
      endHour: this.timeNow.getHours(),
      endMinute: this.timeNow.getMinutes(),
      institution: null
    });

    this.secondForm.reset({
      client: '',
      clientList: [],
      assistancePlanList: [],
      goalList: [],
      hourType: null
    });

    this.thirdForm.reset({
      categoryList: [],
      title: '',
      content: '',
      unfinished: false,
      groupService: false
    });

    this.filteredAssistancePlans = [];
    this.selectedGoals = [];
    this.selectedCategories = [];
    this.clientSelected = false;
    this.assistancePlanSelected = false;
  }
}
