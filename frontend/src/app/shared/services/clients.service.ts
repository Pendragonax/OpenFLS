import { Injectable } from '@angular/core';
import {ClientDto} from "../dtos/client-dto.model";
import {Base} from "./base.service";
import { HttpClient } from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {ClientSoloDto} from "../dtos/client-solo-dto.model";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ClientsService extends Base<ClientDto>{
  url = "clients";

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

  getAllClientSoloDTOs(): Observable<ClientSoloDto[]> {
    return this.http
      .get<ClientSoloDto[]>(`${environment.api_url}${this.url}/solo`)
      .pipe(map(data => data.map(item => this.transformToClientSoloDto(item)))
      );
  }

  transformToClientSoloDto(data: any): ClientSoloDto {
    return {
      id: data.id,
      firstName: data.firstName,
      lastName: data.lastName,
      phoneNumber: data.phoneNumber,
      email: data.email
    };
  }
}
