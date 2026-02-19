import { Component, DestroyRef, inject } from '@angular/core'
import {UserService} from "./shared/services/user.service";
import {Router} from "@angular/router";
import {EmployeeDto} from "./shared/dtos/employee-dto.model";
import {interval, ReplaySubject} from "rxjs";
import {TokenStorageService} from "./shared/services/token.storage.service";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    standalone: false
})

export class AppComponent {
  private readonly destroyRef = inject(DestroyRef);
  public isMenuCollapsed = true;
  test = false;

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
    this.userService.isAuthenticated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: (auth) => {
        this.isAuthenticated = auth;

        if (!auth) {
          this.router.navigate(["/login"]).then();
        }
      }
    });

    this.tokenService.expireTimeString$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => this.timeLeft$.next(value));

    this.userService.user$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
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
