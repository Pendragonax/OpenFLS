import {Directive, Input, OnDestroy, OnInit, TemplateRef, ViewContainerRef} from '@angular/core';
import {UserService} from "../shared/services/user.service";
import {Subject, takeUntil} from "rxjs";

@Directive({
    selector: '[showOnRole]',
    standalone: false
})
export class ShowOnRoleDirective implements OnInit, OnDestroy {

  @Input() showOnRole: string = "";

  stop$ = new Subject();
  isVisible = false;

  constructor(
    private viewContainerRef: ViewContainerRef,
    private templateRef: TemplateRef<any>,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    // clear (hide) container when user has no necessary roles
    this.userService.roles$
      .pipe(takeUntil(this.stop$))
      .subscribe(roles => {
        if (!roles) {
          this.viewContainerRef.clear();
        }

        if (roles.includes(this.showOnRole)) {
          if (!this.isVisible) {
            this.isVisible = true;
            this.viewContainerRef.createEmbeddedView(this.templateRef)
          }
        } else {
          this.isVisible = false;
          this.viewContainerRef.clear();
        }
      });
  }

  ngOnDestroy(): void {
    this.stop$.next(1);
  }
}
