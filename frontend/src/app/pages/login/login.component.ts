import { Component, OnInit } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup } from "@angular/forms";
import { UserService } from "../../services/user.service";
import packageJson from "../../../../package.json";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  isSubmitting = false;
  loginFailed = false;
  version: string = packageJson.version;

  loginForm = new UntypedFormGroup({
    username: new UntypedFormControl(''),
    password: new UntypedFormControl('')
  });

  constructor(
    private userService: UserService
  ) { }

  ngOnInit(): void { }

  handleSuccess() {
    this.isSubmitting = false;
  }

  handleFailure() {
    this.isSubmitting = false;
    this.loginFailed = true;
  }

  login() {
    this.isSubmitting = true;
    this.loginFailed = false;

    this.userService
      .login(this.loginForm.controls['username'].value, this.loginForm.controls['password'].value)
      .subscribe({
        next: () => this.handleSuccess(),
        error: () => this.handleFailure()
      });
  }
}
