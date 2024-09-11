import { Injectable } from '@angular/core';
import {Base} from "./base.service";
import {SponsorDto} from "../dtos/sponsor-dto.model";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class SponsorService extends Base<SponsorDto> {
  url = "sponsors";

  constructor(
    protected override http: HttpClient
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
      this.allValues = values;
    });
  }
}
