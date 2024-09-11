import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {InstitutionDto} from "../dtos/institution-dto.model";
import {Base} from "./base.service";
import {Observable} from "rxjs";
import {InstitutionSoloDto} from "../dtos/institution-solo-dto.model";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class InstitutionService extends Base<InstitutionDto> {
  url = "institutions";

  constructor(
    protected override http: HttpClient
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
    });
  }

  getAllReadable(): Observable<InstitutionSoloDto[]> {
    return this.http
      .get<InstitutionSoloDto[]>(`${environment.api_url}${this.url}/readable`);
  }
}
