import { Component, OnInit } from '@angular/core';
import {ReplaySubject} from "rxjs";
import {UserService} from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Location} from "@angular/common";

@Component({
  selector: 'app-service-client',
  templateUrl: './service-client.component.html',
  styleUrls: ['./service-client.component.css']
})
export class ServiceClientComponent implements OnInit {

  clientId$: ReplaySubject<number> = new ReplaySubject<number>();

  constructor(
    private route: ActivatedRoute,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.loadClientId();
  }

  loadClientId() {
    this.route.paramMap.subscribe(value => {
      try {
        this.clientId$.next(+(value.get('id') ?? ""));
      } catch (e) {
        this.location.back();
      }
    })
  }
}
