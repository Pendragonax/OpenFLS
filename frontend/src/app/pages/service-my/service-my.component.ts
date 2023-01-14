import { Component, OnInit } from '@angular/core';
import {UserService} from "../../services/user.service";
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-service-my',
  templateUrl: './service-my.component.html',
  styleUrls: ['./service-my.component.css']
})
export class ServiceMyComponent implements OnInit {

  userId$: ReplaySubject<number> = new ReplaySubject<number>();

  constructor(
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.loadUserId();
  }

  loadUserId() {
    this.userService.user$.subscribe(value => this.userId$.next(value.id));
  }
}
