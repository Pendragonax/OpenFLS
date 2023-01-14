import { Component, OnInit } from '@angular/core';
import {ReplaySubject} from "rxjs";
import {ActivatedRoute} from "@angular/router";
import {Location} from "@angular/common";

@Component({
  selector: 'app-service-employee',
  templateUrl: './service-employee.component.html',
  styleUrls: ['./service-employee.component.css']
})
export class ServiceEmployeeComponent implements OnInit {

  employeeId$: ReplaySubject<number> = new ReplaySubject<number>();

  constructor(
    private route: ActivatedRoute,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.loadEmployeeId();
  }

  loadEmployeeId() {
    this.route.paramMap.subscribe(value => {
      try {
        this.employeeId$.next(+(value.get('id') ?? ""));
      } catch (e) {
        this.location.back();
      }
    })
  }
}
