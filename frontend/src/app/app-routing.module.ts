import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { HomeComponent } from "./pages/home/home.component";
import {AuthGuard} from "./core/auth.guard";
import {EmployeesComponent} from "./pages/employees/employees.component";
import {EmployeeNewComponent} from "./pages/employees/components/employee-new/employee-new.component";
import {InstitutionComponent} from "./pages/institution/institution.component";
import {EmployeeDetailComponent} from "./pages/employees/components/employee-detail/employee-detail.component";
import {InstitutionNewComponent} from "./pages/institution/components/institution-new/institution-new.component";
import {InstitutionDetailComponent} from "./pages/institution/components/institution-detail/institution-detail.component";
import {CategoryComponent} from "./pages/category/category.component";
import {CategoryDetailComponent} from "./pages/category/components/category-detail/category-detail.component";
import {CategoryNewComponent} from "./pages/category/components/category-new/category-new.component";
import {SponsorComponent} from "./pages/sponsor/sponsor.component";
import {SponsorDetailComponent} from "./pages/sponsor/components/sponsor-detail/sponsor-detail.component";
import {ClientComponent} from "./pages/client/client.component";
import {ClientNewComponent} from "./pages/client/components/client-new/client-new.component";
import {ClientDetailComponent} from "./pages/client/components/client-detail/client-detail.component";
import {AssistancePlanNewComponent} from "./shared/components/assistance-plans/components/assistance-plan-new/assistance-plan-new.component";
import {AssistancePlanDetailComponent} from "./shared/components/assistance-plans/components/assistance-plan-detail/assistance-plan-detail.component";
import {HourTypeComponent} from "./pages/hour-type/hour-type.component";
import {ServiceDetailComponent} from "./pages/service-detail/service-detail.component";
import {
  ServiceEvaluationOverviewComponent
} from "./pages/service-evaluation-overview/service-evaluation-overview.component";
import {AssistancePlanAnalysisComponent} from "./pages/assistance-plan-analysis/assistance-plan-analysis.component";
import {MyServicesComponent} from "./pages/my-services/my-services.component";
import {AllServicesComponent} from "./pages/all-services/all-services.component";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'employees', component: EmployeesComponent, canActivate: [AuthGuard] },
  { path: 'employees/new', component: EmployeeNewComponent, canActivate: [AuthGuard] },
  { path: 'employees/detail/:id', component: EmployeeDetailComponent, canActivate: [AuthGuard] },
  { path: 'institutions', component: InstitutionComponent, canActivate: [AuthGuard] },
  { path: 'institutions/new', component: InstitutionNewComponent, canActivate: [AuthGuard] },
  { path: 'institutions/detail/:id', component: InstitutionDetailComponent, canActivate: [AuthGuard] },
  { path: 'categories', component: CategoryComponent, canActivate: [AuthGuard] },
  { path: 'category/new', component: CategoryNewComponent, canActivate: [AuthGuard] },
  { path: 'category/detail/:id', component: CategoryDetailComponent, canActivate: [AuthGuard] },
  { path: 'sponsors', component: SponsorComponent, canActivate: [AuthGuard] },
  { path: 'sponsors/detail/:id', component: SponsorDetailComponent, canActivate: [AuthGuard] },
  { path: 'clients', component: ClientComponent, canActivate: [AuthGuard] },
  { path: 'clients/new', component: ClientNewComponent, canActivate: [AuthGuard] },
  { path: 'clients/detail/:id', component: ClientDetailComponent, canActivate: [AuthGuard] },
  { path: 'assistance_plans/new/:id', component: AssistancePlanNewComponent, canActivate: [AuthGuard] },
  { path: 'assistance_plans/detail/:id', component: AssistancePlanDetailComponent, canActivate: [AuthGuard] },
  { path: 'assistance_plans/detail/:id/:tab', component: AssistancePlanDetailComponent, canActivate: [AuthGuard] },
  { path: 'assistance_plans/analysis/:id', component: AssistancePlanAnalysisComponent, canActivate: [AuthGuard] },
  { path: 'assistance_plans/analysis/:id/:tab', component: AssistancePlanAnalysisComponent, canActivate: [AuthGuard] },
  { path: 'hour_types', component: HourTypeComponent, canActivate: [AuthGuard] },
  { path: 'services/my', component: MyServicesComponent, canActivate: [AuthGuard] },
  { path: 'services/my/:start/:end', component: MyServicesComponent, canActivate: [AuthGuard] },
  { path: 'services/all', component: AllServicesComponent, canActivate: [AuthGuard] },
  { path: 'services/all/:start/:end/:institutionId/:employeeId/:clientId', component: AllServicesComponent, canActivate: [AuthGuard] },
  { path: 'services/new', component: ServiceDetailComponent, canActivate: [AuthGuard] },
  { path: 'services/new/:date', component: ServiceDetailComponent, canActivate: [AuthGuard] },
  { path: 'services/edit/:id', component: ServiceDetailComponent, canActivate: [AuthGuard] },
  { path: 'overview', component: ServiceEvaluationOverviewComponent, canActivate: [AuthGuard] },
  { path: 'overview/:year/:month/:hourTypeId/:areaId/:sponsorId/:valueTypeId', component: ServiceEvaluationOverviewComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [
    AuthGuard
  ]
})
export class AppRoutingModule { }
