import {AbstractControl, ValidationErrors, ValidatorFn} from "@angular/forms";

/**
 * This function checks in a FormGroup containing the controls 'password1' and 'password2' if these two controls
 * got the same value (same password [string]).
 * @return null = values match, {invalidPassword: true} = values doesnt match
* */
export function createMatchingPasswordsValidator(control:AbstractControl) : ValidationErrors | null {
  if (!control.get('password1')?.value || !control.get('password2')?.value) { return null }
  if (control.get('password1')?.value == control.get('password2')?.value) { return null }

  return { invalidPassword: true }
}
