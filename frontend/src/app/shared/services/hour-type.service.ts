import { Injectable } from '@angular/core';
import {HourTypeDto} from "../dtos/hour-type-dto.model";
import {Base} from "./base.service";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class HourTypeService extends Base<HourTypeDto> {
  url = "hour_types";

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
