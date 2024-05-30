import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import {UserService} from "../services/user.service";
import {map} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard  {
  isAuthenticated: boolean = false;

  constructor(
    protected router: Router,
    private userService: UserService
  ) {
    this.userService.isAuthenticated$.subscribe(value => this.isAuthenticated = value);
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot)
  {
    if (this.isAuthenticated) {
      return true;
    }

    this.router.navigate(["/login"]).then(() => window.location.reload());
    return false;
  }
}
