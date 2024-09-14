import { Injectable } from '@angular/core';
import {Base} from "./base.service";
import {EmployeeDto} from "../dtos/employee-dto.model";
import {GoalDto} from "../dtos/goal-dto.model";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class GoalService extends Base<GoalDto> {
  url = "goals";

  constructor(
    protected override http: HttpClient
  ) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
  }
}
