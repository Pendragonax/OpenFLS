import { Injectable } from '@angular/core';
import {ClientDto} from "../dtos/client-dto.model";
import {Base} from "./base.service";
import {HttpClient} from "@angular/common/http";

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
}
