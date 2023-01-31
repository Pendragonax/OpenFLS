import {AbstractControl, ValidationErrors} from "@angular/forms";

/**
 * This function checks in a FormGroup containing the controls 'start' and 'end' if the end date is
 * after the start date.
 * @return null = okay, {invalidEnd: true} = end <= start
 * */
export function createStartEndValidator(control:AbstractControl) : ValidationErrors | null {
  if (!control.get('start')?.value || !control.get('end')?.value) { return null }

  try {
    const startDate = new Date(control.get('start')?.value);
    const endDate = new Date(control.get('end')?.value);

    return (endDate > startDate) ? null : { invalidEnd: true };
  } catch(e) {
    return { invalidEnd: true }
  }
}
