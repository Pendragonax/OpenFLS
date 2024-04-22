import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { AppComponent } from './app.component';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AuthInterceptor} from "./interceptors/auth.interceptor";
import { HomeComponent } from './pages/home/home.component';
import { UserService } from "./services/user.service";
import {TokenStorageService} from "./services/token.storage.service";
import { EmployeesComponent } from './pages/employees/employees.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import { EmployeeNewComponent } from './pages/employee-new/employee-new.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatStepperModule} from "@angular/material/stepper";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import { InstitutionComponent } from './pages/institution/institution.component';
import {MatTableModule} from "@angular/material/table";
import {MatCardModule} from "@angular/material/card";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatTooltipModule} from "@angular/material/tooltip";
import { EmployeeDetailComponent } from './pages/employee-detail/employee-detail.component';
import {MatListModule} from "@angular/material/list";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatExpansionModule} from "@angular/material/expansion";
import { InstitutionNewComponent } from './pages/institution-new/institution-new.component';
import { InstitutionDetailComponent } from './pages/institution-detail/institution-detail.component';
import { CategoryComponent } from './pages/category/category.component';
import { CategoryDetailComponent } from './pages/category-detail/category-detail.component';
import {MatSortModule} from "@angular/material/sort";
import { CategoryNewComponent } from './pages/category-new/category-new.component';
import { InfoHeaderComponent } from './shared/modules/info-header/info-header.component';
import { ContingentsComponent } from './shared/modules/contingents/contingents.component';
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import { ShowOnRoleDirective } from './directives/show-on-role.directive';
import { SponsorComponent } from './pages/sponsor/sponsor.component';
import { SponsorDetailComponent } from './pages/sponsor-detail/sponsor-detail.component';
import { ClientComponent } from './pages/client/client.component';
import { ClientNewComponent } from './pages/client-new/client-new.component';
import { ClientDetailComponent } from './pages/client-detail/client-detail.component';
import { AssistancePlansComponent } from './shared/modules/assistance-plans/assistance-plans.component';
import { AssistancePlanNewComponent } from './pages/assistance-plan-new/assistance-plan-new.component';
import { AssistancePlanDetailComponent } from './pages/assistance-plan-detail/assistance-plan-detail.component';
import { GoalsComponent } from './shared/modules/goals/goals.component';
import { HourTypeComponent } from './pages/hour-type/hour-type.component';
import { AssistancePlanHoursComponent } from './shared/modules/assistance-plan-hours/assistance-plan-hours.component';
import { ServiceComponent } from './shared/modules/service/service.component';
import { ServiceDetailComponent } from './pages/service-detail/service-detail.component';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatPaginatorIntl, MatPaginatorModule} from "@angular/material/paginator";
import { UnprofesssionalComponent } from './shared/modules/unprofesssional/unprofesssional.component';
import {getGermanPaginatorIntl} from "./intl/german-paginator-intl";
import { ServiceMyComponent } from './pages/service-my/service-my.component';
import { ServiceClientComponent } from './pages/service-client/service-client.component';
import { ServiceEmployeeComponent } from './pages/service-employee/service-employee.component';
import { InformationRowComponent } from './shared/layouts/information-row/information-row.component';
import { WorkTimeCardComponent } from './shared/modules/contingent-evaluation/work-time-card/work-time-card.component';
import { AssistancePlanEvaluationComponent } from './shared/modules/assistance-plan-evaluation/assistance-plan-evaluation.component';
import {MatRadioModule} from "@angular/material/radio";
import { AssistancePlanAnalysisComponent } from './shared/modules/assistance-plan-analysis/assistance-plan-analysis.component';
import { OverviewTableComponent } from './components/overview-table/overview-table.component';
import { ServiceEvaluationOverviewComponent } from './pages/service-evaluation-overview/service-evaluation-overview.component';
import { OverviewValueTypeInfoModalComponent } from './modals/overview-valuetype-info-modal/overview-value-type-info-modal.component';
import { MatDialogModule} from "@angular/material/dialog";
import { OverviewPermissionInfoModalComponent } from './modals/overview-permission-info-modal/overview-permission-info-modal.component';
import { GoalEvaluationComponent } from './pages/goal-evaluation/goal-evaluation.component';
import { GoalSingleComponent } from './components/goal-single/goal-single.component';
import {MatChipsModule} from "@angular/material/chips";
import { YearMonthSelectionComponent } from './components/year-month-selection/year-month-selection.component';
import { GoalTimeEvaluationFilterComponent } from './pages/goal-evaluation/components/goal-time-evaluation-filter/goal-time-evaluation-filter.component';
import { TableButtonComponent } from './components/table-button/table-button.component';
import { GoalEvaluationFilterComponent } from './pages/goal-evaluation/components/goal-evaluation-filter/goal-evaluation-filter.component';
import { GoalEvaluationModalComponent } from './modals/goal-evaluation-modal/goal-evaluation-modal.component';
import { ConfirmationModalComponent } from './modals/confirmation-modal/confirmation-modal.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { ErrorIconComponent } from './components/error-icon/error-icon.component';
import { ObjectTableComponent } from './components/object-table/object-table.component';
import {ContingentEvaluationComponent} from "./shared/modules/contingent-evaluation/contingent-evaluation.component";
import {ContingentOverviewComponent} from "./domains/contingent-overviews/contingent-overview.component";

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: ''},
  { path: 'login', component: LoginComponent}
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
    ServiceComponent,
    ServiceDetailComponent,
    UnprofesssionalComponent,
    ServiceMyComponent,
    ServiceClientComponent,
    ServiceEmployeeComponent,
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
    ContingentOverviewComponent
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
    MatChipsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: MatPaginatorIntl, useValue: getGermanPaginatorIntl() },
    TokenStorageService,
    UserService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
