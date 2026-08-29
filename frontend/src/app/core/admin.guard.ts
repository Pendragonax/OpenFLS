import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {UserService} from '../shared/services/user.service';
import {map, take} from 'rxjs';

@Injectable({providedIn: 'root'})
export class AdminGuard {
  constructor(private userService: UserService, private router: Router) {}

  canActivate() {
    return this.userService.isAdmin$.pipe(take(1), map(isAdmin => {
      if (!isAdmin) this.router.navigate(['/']);
      return isAdmin;
    }));
  }
}
