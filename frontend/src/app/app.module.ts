import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {AppRoutingModule} from './app-routing.module';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './pages/login/login.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AuthInterceptor} from "./interceptors/auth.interceptor";
import {HomeComponent} from './pages/home/home.component';
import {UserService} from "./shared/services/user.service";
import {TokenStorageService} from "./shared/services/token.storage.service";
import {EmployeesComponent} from './pages/employees/employees.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {EmployeeNewComponent} from './pages/employees/components/employee-new/employee-new.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatStepperModule} from "@angular/material/stepper";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {InstitutionComponent} from './pages/institution/institution.component';
import {MatTableModule} from "@angular/material/table";
import {MatCardModule} from "@angular/material/card";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatTooltipModule} from "@angular/material/tooltip";
import {EmployeeDetailComponent} from './pages/employees/components/employee-detail/employee-detail.component';
import {MatListModule} from "@angular/material/list";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatExpansionModule} from "@angular/material/expansion";
import {InstitutionNewComponent} from './pages/institution/components/institution-new/institution-new.component';
import {
  InstitutionDetailComponent
} from './pages/institution/components/institution-detail/institution-detail.component';
import {CategoryComponent} from './pages/category/category.component';
import {CategoryDetailComponent} from './pages/category/components/category-detail/category-detail.component';
import {MatSortModule} from "@angular/material/sort";
import {CategoryNewComponent} from './pages/category/components/category-new/category-new.component';
import {InfoHeaderComponent} from './shared/components/info-header/info-header.component';
import {ContingentsComponent} from './shared/components/contingents/contingents.component';
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {ShowOnRoleDirective} from './directives/show-on-role.directive';
import {SponsorComponent} from './pages/sponsor/sponsor.component';
import {SponsorDetailComponent} from './pages/sponsor/components/sponsor-detail/sponsor-detail.component';
import {ClientComponent} from './pages/client/client.component';
import {ClientNewComponent} from './pages/client/components/client-new/client-new.component';
import {ClientDetailComponent} from './pages/client/components/client-detail/client-detail.component';
import {AssistancePlansComponent} from './shared/components/assistance-plans/assistance-plans.component';
import {
  AssistancePlanNewComponent
} from './shared/components/assistance-plans/components/assistance-plan-new/assistance-plan-new.component';
import {
  AssistancePlanDetailComponent
} from './shared/components/assistance-plans/components/assistance-plan-detail/assistance-plan-detail.component';
import {
  GoalsComponent
} from './shared/components/assistance-plans/components/assistance-plan-detail/components/goals/goals.component';
import {HourTypeComponent} from './pages/hour-type/hour-type.component';
import {
  AssistancePlanHoursComponent
} from './shared/components/assistance-plans/components/assistance-plan-hours/assistance-plan-hours.component';
import {ServiceDetailComponent} from './pages/service-detail/service-detail.component';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatPaginatorIntl, MatPaginatorModule} from "@angular/material/paginator";
import {UnprofesssionalComponent} from './shared/components/unprofesssional/unprofesssional.component';
import {getGermanPaginatorIntl} from "./intl/german-paginator-intl";
import {InformationRowComponent} from './shared/components/information-row/information-row.component';
import {
  WorkTimeCardComponent
} from './pages/employees/components/employee-detail/contingent-evaluation/work-time-card/work-time-card.component';
import {
  AssistancePlanEvaluationComponent
} from './shared/components/assistance-plans/components/assistance-plan-evaluation/assistance-plan-evaluation.component';
import {MatRadioModule} from "@angular/material/radio";
import {
  AssistancePlanAnalysisComponent
} from './shared/components/assistance-plans/components/assistance-plan-analysis/assistance-plan-analysis.component';
import {OverviewTableComponent} from './shared/components/overview-table/overview-table.component';
import {
  ServiceEvaluationOverviewComponent
} from './pages/service-evaluation-overview/service-evaluation-overview.component';
import {
  OverviewValueTypeInfoModalComponent
} from './pages/service-evaluation-overview/modals/overview-valuetype-info-modal/overview-value-type-info-modal.component';
import {MatDialogModule} from "@angular/material/dialog";
import {
  OverviewPermissionInfoModalComponent
} from './pages/service-evaluation-overview/modals/overview-permission-info-modal/overview-permission-info-modal.component';
import {GoalEvaluationComponent} from './pages/goal-evaluation/goal-evaluation.component';
import {GoalSingleComponent} from './shared/components/goal-single/goal-single.component';
import {MatChipsModule} from "@angular/material/chips";
import {YearMonthSelectionComponent} from './shared/components/year-month-selection/year-month-selection.component';
import {
  GoalTimeEvaluationFilterComponent
} from './pages/goal-evaluation/components/goal-time-evaluation-filter/goal-time-evaluation-filter.component';
import {TableButtonComponent} from './shared/components/table-button/table-button.component';
import {
  GoalEvaluationFilterComponent
} from './pages/goal-evaluation/components/goal-evaluation-filter/goal-evaluation-filter.component';
import {
  GoalEvaluationModalComponent
} from './pages/goal-evaluation/modals/goal-evaluation-modal/goal-evaluation-modal.component';
import {ConfirmationModalComponent} from './shared/modals/confirmation-modal/confirmation-modal.component';
import {LoadingSpinnerComponent} from './shared/components/loading-spinner/loading-spinner.component';
import {ErrorIconComponent} from './shared/components/error-icon/error-icon.component';
import {ObjectTableComponent} from './shared/components/object-table/object-table.component';
import {
  ContingentEvaluationComponent
} from "./pages/employees/components/employee-detail/contingent-evaluation/contingent-evaluation.component";
import {
  ContingentOverviewComponent
} from "./pages/institution/components/institution-detail/contingent-overviews/contingent-overview.component";
import {SearchFieldComponent} from "./shared/components/search-field/search-field.component";
import {MyServicesComponent} from "./pages/my-services/my-services.component";
import {InstitutionSelectComponent} from "./shared/components/institution-select/institution-select.component";
import {
  DateCompleteSelectionComponent
} from "./shared/components/date-complete-selection/date-complete-selection.component";
import {AllServicesComponent} from "./pages/all-services/all-services.component";
import {ClientAutocompleteComponent} from "./shared/components/client-autocomplete/client-autocomplete.component";
import {ServiceTableComponent} from "./shared/components/service-table/service-table.component";
import {EmployeeAutocompleteComponent} from "./shared/components/employee-autocomplete/employee-autocomplete.component";

const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: ''},
  {path: 'login', component: LoginComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    EmployeesComponent,
    EmployeeNewComponent,
    InstitutionComponent,
    EmployeeDetailComponent,
    InstitutionNewComponent,
    InstitutionDetailComponent,
    CategoryComponent,
    CategoryDetailComponent,
    CategoryNewComponent,
    InfoHeaderComponent,
    ContingentsComponent,
    ShowOnRoleDirective,
    SponsorComponent,
    SponsorDetailComponent,
    ClientComponent,
    ClientNewComponent,
    ClientDetailComponent,
    AssistancePlansComponent,
    AssistancePlanNewComponent,
    AssistancePlanDetailComponent,
    GoalsComponent,
    HourTypeComponent,
    AssistancePlanHoursComponent,
    ServiceDetailComponent,
    UnprofesssionalComponent,
    InformationRowComponent,
    ContingentEvaluationComponent,
    WorkTimeCardComponent,
    AssistancePlanEvaluationComponent,
    AssistancePlanAnalysisComponent,
    OverviewTableComponent,
    ServiceEvaluationOverviewComponent,
    OverviewValueTypeInfoModalComponent,
    OverviewPermissionInfoModalComponent,
    GoalEvaluationComponent,
    GoalSingleComponent,
    YearMonthSelectionComponent,
    GoalTimeEvaluationFilterComponent,
    TableButtonComponent,
    GoalEvaluationFilterComponent,
    GoalEvaluationModalComponent,
    ConfirmationModalComponent,
    LoadingSpinnerComponent,
    ErrorIconComponent,
    ObjectTableComponent,
    ContingentEvaluationComponent,
    ContingentOverviewComponent,
    MyServicesComponent,
    AllServicesComponent,
    ClientAutocompleteComponent,
    ServiceTableComponent
  ],
  imports: [
    RouterModule.forRoot(routes),
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    BrowserAnimationsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatStepperModule,
    MatInputModule,
    MatTabsModule,
    MatCheckboxModule,
    MatSelectModule,
    MatTableModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatListModule,
    MatSidenavModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatSortModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatAutocompleteModule,
    MatToolbarModule,
    MatPaginatorModule,
    MatRadioModule,
    MatDialogModule,
    MatChipsModule,
    SearchFieldComponent,
    InstitutionSelectComponent,
    DateCompleteSelectionComponent,
    EmployeeAutocompleteComponent
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: MatPaginatorIntl, useValue: getGermanPaginatorIntl()},
    TokenStorageService,
    UserService,
  ],
  exports: [
    ShowOnRoleDirective
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
