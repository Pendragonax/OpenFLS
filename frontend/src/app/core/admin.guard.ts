import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from '../shared/services/user.service';
import {map, take} from 'rxjs';

@Injectable({providedIn: 'root'})
export class AdminGuard {
  constructor(private userService: UserService, private router: Router) {}

  canActivate() {
    // user$ emits only after the asynchronous /user request has completed.
    // Waiting for it prevents a page reload from treating the initial
    // `isAdmin$ = false` value as a real denial for an administrator.
    return this.userService.user$.pipe(take(1), map(user => {
      const isAdmin = (user.access?.role ?? 99) === 1;
      if (!isAdmin) this.router.navigate(['/']);
      return isAdmin;
    }));
  }
}
