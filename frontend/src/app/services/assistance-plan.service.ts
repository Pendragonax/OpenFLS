import { Injectable } from '@angular/core';
import {Base} from "./base.service";
import {AssistancePlanDto} from "../dtos/assistance-plan-dto.model";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {InstitutionDto} from "../dtos/institution-dto.model";
import {combineLatest, map} from "rxjs";
import {ClientDto} from "../dtos/client-dto.model";
import {InstitutionService} from "./institution.service";
import {ClientsService} from "./clients.service";
import {UserService} from "./user.service";
import {AssistancePlanView} from "../models/assistance-plan-view.model";
import {SponsorService} from "./sponsor.service";
import {SponsorDto} from "../dtos/sponsor-dto.model";

@Injectable({
  providedIn: 'root'
})
export class AssistancePlanService extends Base<AssistancePlanDto>{
  url = "assistance_plans";

  constructor(
    protected override http: HttpClient,
    private institutionService: InstitutionService,
    private clientService: ClientsService,
    private userService: UserService,
    private sponsorService: SponsorService
  ) {
    super(http);
  }

  override initialLoad() {
  }

  getByClientId(id: number): Observable<AssistancePlanDto[]> {
    return this.http
      .get<AssistancePlanDto[]>(`${environment.api_url}${this.url}/client/${id}`)
  }

  getCombinationByClientId(id: number): Observable<[ClientDto, InstitutionDto, SponsorDto, AssistancePlanView][]> {
    return combineLatest([
      this.clientService.getById(id),
      this.institutionService.allValues$,
      this.sponsorService.allValues$,
      this.getByClientId(id),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$]
    ).pipe(map(([client, institutions, sponsors, assistancePlans, affiliatedInstitutions, isAdmin]) => {
      return assistancePlans.map<[ClientDto, InstitutionDto, SponsorDto, AssistancePlanView]>(plan => {
        return [
          client,
          institutions.find(x => x.id === plan.institutionId) ?? new InstitutionDto(),
          sponsors.find(x => x.id === plan.sponsorId) ?? new SponsorDto(),
          <AssistancePlanView> {
            dto: plan,
            editable: isAdmin || affiliatedInstitutions.some(value => value === plan.institutionId)
        }]
      })
    }))
  }

  getByInstitutionId(id: number): Observable<AssistancePlanDto[]> {
    return this.http
      .get<AssistancePlanDto[]>(`${environment.api_url}${this.url}/institution/${id}`)
  }

  getCombinationByInstitutionId(id: number): Observable<[ClientDto, InstitutionDto, SponsorDto, AssistancePlanView][]> {
    return combineLatest([
      this.clientService.allValues$,
      this.institutionService.getById(id),
      this.sponsorService.allValues$,
      this.getByInstitutionId(id),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$]
    ).pipe(map(([clients, institution, sponsors, assistancePlans, affiliatedInstitutions, isAdmin]) => {
      return assistancePlans.map<[ClientDto, InstitutionDto, SponsorDto, AssistancePlanView]>(plan => {
        return [
          clients.find(x => x.id === plan.clientId) ?? new ClientDto(),
          institution,
          sponsors.find(x => x.id === plan.sponsorId) ?? new SponsorDto(),
          <AssistancePlanView> {
            dto: plan,
            editable: isAdmin || affiliatedInstitutions.some(value => value === plan.institutionId)
        }]
      })
    }))
  }

  getBySponsorId(id: number): Observable<AssistancePlanDto[]> {
    return this.http
      .get<AssistancePlanDto[]>(`${environment.api_url}${this.url}/sponsor/${id}`)
  }

  getCombinationBySponsorId(id: number): Observable<[ClientDto, InstitutionDto, SponsorDto, AssistancePlanView][]> {
    return combineLatest([
      this.clientService.allValues$,
      this.institutionService.allValues$,
      this.sponsorService.getById(id),
      this.getBySponsorId(id),
      this.userService.affiliatedInstitutions$,
      this.userService.isAdmin$]
    ).pipe(map(([clients, institutions, sponsor, assistancePlans, affiliatedInstitutions, isAdmin]) => {
      return assistancePlans.map<[ClientDto, InstitutionDto, SponsorDto, AssistancePlanView]>(plan => {
        return [
          clients.find(x => x.id === plan.clientId) ?? new ClientDto(),
          institutions.find(x => x.id === plan.institutionId) ?? new InstitutionDto(),
          sponsor,
          <AssistancePlanView> {
            dto: plan,
            editable : isAdmin || affiliatedInstitutions.some(value => value === plan.institutionId)
        }]
      })
    }))
  }
}
