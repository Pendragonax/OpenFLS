import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class Converter {
  private static padTo2Digits(num: number) {
    return num.toString().padStart(2, '0');
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

  getDateTimeString(date: string, hour: number, minute: number): string {
    const hourString = `${this.addLeadingZeros(hour, 2)}`;
    const minuteString = `${this.addLeadingZeros(minute, 2)}`;
    const time = `T${hourString}:${minuteString}:00.000`
    return `${date}${time}`
  }

  /**
   * Converts date to a string with YYYY-MM-DD pattern
   * @param date
   * @return string with format YYYY-MM-DD
   */
  formatToTime(date: Date) {
    return (
      [
        Converter.padTo2Digits(date.getHours()),
        Converter.padTo2Digits(date.getMinutes()),
      ].join(':')
    );
  }

  convertToTimeString(ms: number) {
    return (
      [
        Converter.padTo2Digits(Math.floor((ms / (1000 * 60 * 60)) % 24)),
        Converter.padTo2Digits(Math.floor((ms / (1000 * 60)) % 60)),
        Converter.padTo2Digits(Math.floor((ms / 1000) % 60))
      ].join(':')
    );
  }

  addLeadingZeros(num: number, length: number): string {
    return String(num).padStart(length, '0');
  }

  convertToEuro(num: number): string {
    return `${this.convertToDecimalString(num)}€`
  }

  convertToDecimalString(num: number): string {
    return num.toFixed(2);
  }

  getLocalDateString(date: string | null) : string {
    if (date === null)
      return "unbegrenzt";

    return this.formatDateToGerman(new Date(date));
  }
}
