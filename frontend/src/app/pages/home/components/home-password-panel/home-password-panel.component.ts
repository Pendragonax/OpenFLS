import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AbstractControl, FormControl, FormGroup} from "@angular/forms";

type PasswordForm = FormGroup<{
  oldPassword: FormControl<string>;
  password1: FormControl<string>;
  password2: FormControl<string>;
}>;

@Component({
  selector: 'app-home-password-panel',
  templateUrl: './home-password-panel.component.html',
  styleUrls: ['./home-password-panel.component.css'],
  standalone: false
})
export class HomePasswordPanelComponent {
  @Input({ required: true }) form!: PasswordForm;
  @Input({ required: true }) isSubmitting!: boolean;
  @Output() submitPassword = new EventEmitter<void>();

  showOldPassword = false;
  showNewPassword = false;
  showRepeatPassword = false;

  get oldPasswordControl() {
    return this.form.controls.oldPassword;
  }

  get password1Control() {
    return this.form.controls.password1;
  }

  get password2Control() {
    return this.form.controls.password2;
  }

  toggleOldPasswordVisibility() {
    this.showOldPassword = !this.showOldPassword;
  }

  toggleNewPasswordVisibility() {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleRepeatPasswordVisibility() {
    this.showRepeatPassword = !this.showRepeatPassword;
  }

  getControlErrorMessage(control: AbstractControl | null) {
    if (control?.hasError('required')) {
      return 'Eingabe ist notwendig';
    }
    if (control?.hasError('minlength')) {
      return 'zu wenig Zeichen';
    }
    if (control?.hasError('pattern')) {
      return 'Großbuchtabe, Zahl und Sonderzeichen notwendig';
    }

    return 'unbekannter Fehler';
  }
}
