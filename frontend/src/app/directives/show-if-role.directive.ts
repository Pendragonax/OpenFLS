import {Directive, Input, OnInit, TemplateRef, ViewContainerRef} from '@angular/core';
import {UserService} from "../shared/services/user.service";

@Directive({
  selector: '[showIfRole]',
  standalone: true,
  providers: [UserService]
})
export class ShowIfRoleDirective implements OnInit {

  @Input() showIfRole: string = ""

  private hasView = false;

  constructor(private viewContainer: ViewContainerRef,
              private templateRef: TemplateRef<any>,
              private userService: UserService) { }

  ngOnInit(): void {
    // clear (hide) container when user has no necessary roles
    this.userService.roles$
      .subscribe(roles => {
        if (!roles) {
          this.viewContainer.clear();
          this.hasView = false;
        }

        if (roles.includes(this.showIfRole)) {
          if (!this.hasView) {
            this.viewContainer.createEmbeddedView(this.templateRef)
            this.hasView = true;
          }
        } else {
          this.viewContainer.clear();
          this.hasView = false;
        }
      });
  }

}
