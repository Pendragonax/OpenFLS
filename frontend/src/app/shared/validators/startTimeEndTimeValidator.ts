import {AbstractControl, ValidationErrors} from "@angular/forms";

/**
 * This function checks in a FormGroup containing the controls 'start' and 'end' if the end date is
 * after the start date.
 * @return null = okay, {invalidEnd: true} = end <= start
 * */
export function createStartTimeEndTimeValidator(control:AbstractControl) : ValidationErrors | null {
  if (!control.get('startHour')?.value
    || control.get('startMinute')?.value == null
    || control.get('endHour')?.value == null
    || control.get('endMinute')?.value == null
    || control.get('startDate')?.value == null) {
    return null;
  }

  try {
    const startHour = control.get('startHour')?.value as number;
    const startMinute = control.get('startMinute')?.value as number;
    const endHour = control.get('endHour')?.value as number;
    const endMinute = control.get('endMinute')?.value as number;

    return ((startHour == endHour && startMinute >= endMinute) || (startHour > endHour))
      ? { invalidEnd: true } : null;
  } catch(e) {
    return { invalidEnd: true }
  }
}
