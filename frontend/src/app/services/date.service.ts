import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DateService {

  constructor() { }

  public getMonths(initialArray: string[]): string[] {
    let monthArray: string[] = [...initialArray];

    for (let i = 1; i <= 12; i++) {
      monthArray.push(i.toString().padStart(2, '0'));
    }

    return monthArray;
  }

  public formatDateToYearMonthDay(date: Date): string {
    const year = date.getFullYear().toString();
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Months are zero-based
    const day = date.getDate().toString().padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  public getDaysWithInitial(initialArray: string[], year: number, month: number): string[] {
    const daysInMonth = this.getDaysAmountInMonth(year, month);
    let daysArray: string[] = [...initialArray];

    for (let i = 1; i <= daysInMonth; i++) {
      daysArray.push(i.toString().padStart(2, '0'));
    }

    return daysArray;
  }

  public getDays(year: number, month: number): Array<string> {
    return this.getDaysWithInitial([], year, month);
  }

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

  public formatDateString(input: string): string {
    const date = new Date(input);

    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Month is 0-indexed
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');

    return `${day}.${month}.${year} ${hours}:${minutes}`;
  }
}
