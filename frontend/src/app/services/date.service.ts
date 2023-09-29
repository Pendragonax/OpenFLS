import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DateService {

  constructor() { }

  public getDaysAmountInMonth(year: number, month: number): number {
    // Check if the month is valid (1 to 12)
    if (month < 1 || month > 12) {
      throw new Error("Month must be between 1 and 12");
    }

    // Use the Date class to calculate the number of days
    return new Date(year, month, 0).getDate();
  }
}
