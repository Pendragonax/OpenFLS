import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class Converter {
  /**
   * round to 2 digits
   * @param val: number
   * @return number
   */
  roundTo2Digits(val: number) : number {
    return Math.round(val * 100) / 100;
  }

  /**
   * checks if string or number is a number
   * @param value: string | number
   * @return boolean
   */
  isNumber(value: string | number): boolean {
    return ((value != null) &&
      (value !== '') &&
      !isNaN(Number(value.toString())));
  }

  /**
   * Get the initials of each word of string
   * @param string: string
   * @param amount: amount of chars of each word
   * @return string as initials
   */
  getInitials(string: string, amount: number): string {
    return string
      .split(' ')
      .map(value => (value.length > amount - 1) ? value.slice(0, amount) : value)
      .join('');
  }

  /**
   * Determine the days of a specific month
   * @param date: date
   * @return number of days
   */
  getDaysOfMonth(date: Date): number {
    return new Date(date.getFullYear(), date.getMonth(), 0).getDate();
  }

  /**
   * Concat two strings with delimiter when the target is not empty.
   * @param target: string to concat to
   * @param str: string to concat
   * @param delimiter: string
   * @return string
   */
  concatStringIfNotExists(target: string, str: string, delimiter: string): string {
    if (target.length <= 0) {
      return str;
    }

    if (!target.includes(str)) {
      return target + delimiter + str;
    }

    return target;
  }

  /**
   * Converts date to a string with YYYY-MM-DD pattern
   * @param date
   * @return string with format YYYY-MM-DD
   */
  formatDate(date: Date) {
    return (
      [
        date.getFullYear(),
        Converter.padTo2Digits(date.getMonth() + 1),
        Converter.padTo2Digits(date.getDate()),
      ].join('-')
    );
  }

  /**
   * Converts date to a string with YYYY-MM-DD pattern
   * @param date
   * @return string with format YYYY-MM-DD
   */
  formatDateToGerman(date: Date) {
    return (
      [
        Converter.padTo2Digits(date.getDate()),
        Converter.padTo2Digits(date.getMonth() + 1),
        date.getFullYear(),
      ].join('.')
    );
  }

  /**
   * Converts date to a string with YYYY-MM-DD HH:MM pattern
   * @param date
   * @return string with format YYYY-MM-DD HH:MM
   */
  formatDateToGermanTime(date: Date) {
    return (
      [
        Converter.padTo2Digits(date.getDate()),
        Converter.padTo2Digits(date.getMonth() + 1),
        date.getFullYear(),
      ].join('.') + " " +
      this.formatToTime(date)
    );
  }

  /**
   * Converts date to a date time string
   * @param date
   * @return string with format
   */
  getDateTimeString(date: string, hour: number, minute: number): string {
    const hourString = `${this.addLeadingZeros(hour, 2)}`;
    const minuteString = `${this.addLeadingZeros(minute, 2)}`;
    const time = `T${hourString}:${minuteString}:00.000`
    return `${date}${time}`
  }

  /**
   * Converts date to a time string
   * @param date
   * @return string with format HH:MM
   */
  formatToTime(date: Date) {
    return (
      [
        Converter.padTo2Digits(date.getHours()),
        Converter.padTo2Digits(date.getMinutes()),
      ].join(':')
    );
  }

  /**
   * Converts date to a time string HH:MM:SS
   * @param ms: number as milliseconds
   * @return string with format HH:MM:SS
   */
  convertToTimeString(ms: number) {
    return (
      [
        Converter.padTo2Digits(Math.floor((ms / (1000 * 60 * 60)) % 24)),
        Converter.padTo2Digits(Math.floor((ms / (1000 * 60)) % 60)),
        Converter.padTo2Digits(Math.floor((ms / 1000) % 60))
      ].join(':')
    );
  }

  /**
   * Add leading zeros to a number
   * @param num: number to add leading zeros to
   * @param length: size of the number
   * @return string
   */
  addLeadingZeros(num: number, length: number): string {
    return String(num).padStart(length, '0');
  }

  /**
   * Convert number to €
   * @param num: number
   * @return string
   */
  convertToEuro(num: number): string {
    return `${this.convertToDecimalString(num)}€`
  }

  convertToDecimalString(num: number): string {
    return num.toFixed(2);
  }

  /**
   * Convert number to €
   * @param date: string | null
   * @return string null = 'unbegrenzt' else = YYYY.MM.DD
   */
  getLocalDateString(date: string | null) : string {
    if (date === null)
      return "unbegrenzt";

    return this.formatDateToGerman(new Date(date));
  }

  private static padTo2Digits(num: number) {
    return num.toString().padStart(2, '0');
  }
}
