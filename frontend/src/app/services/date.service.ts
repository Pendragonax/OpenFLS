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

  public addMinutesToTime(time: number, minutesToAdd: number): number {
    // Extract the hours and minutes from the input time
    const hours = Math.floor(time);
    const minutes = (time - hours) * 100;

    // Add the minutes to the existing minutes
    const totalMinutes = minutes + minutesToAdd;

    // Calculate the new hours and minutes
    const newHours = hours + Math.floor(totalMinutes / 60);
    const newMinutes = totalMinutes % 60;

    // Combine the new hours and minutes and return as a number with decimals
    return newHours + newMinutes / 100;
  }
}
