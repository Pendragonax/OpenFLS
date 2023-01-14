import { Component } from '@angular/core'
import {UserService} from "./services/user.service";
import {Router} from "@angular/router";
import {EmployeeDto} from "./dtos/employee-dto.model";
import {interval, ReplaySubject} from "rxjs";
import {TokenStorageService} from "./services/token.storage.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent {
  public isMenuCollapsed = true;

  title = 'OpenFLS';
  isAuthenticated: Boolean = false;
  employee: EmployeeDto | null = null;
  role: number = 0

  timeLeft$: ReplaySubject<string> = new ReplaySubject<string>();

  constructor(
    private userService: UserService,
    private tokenService: TokenStorageService,
    private router: Router) { }

  ngOnInit() {
    this.userService.isAuthenticated$.subscribe({
      next: (auth) => {
        this.isAuthenticated = auth;

        if (!auth) {
          this.router.navigate(["/login"]).then();
        }
      }
    });

    this.tokenService.expireTimeString$.subscribe(value => this.timeLeft$.next(value));

    this.userService.user$.subscribe({
      next: (employee) => {
        this.employee = employee

        if (this.employee.access?.role)
          this.role = this.employee.access?.role
      },
      error: () => this.logout()
    } );
  }

  onActivate(event) {
    this.userService.checkAuthentication();
  }

  logout() {
    this.userService.logout();
  }
}
