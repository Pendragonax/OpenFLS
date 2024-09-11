import {PeriodMode} from "./PeriodMode";

export class Period {
  periodMode: PeriodMode = PeriodMode.PERIOD_MODE_YEARLY;
  year: number = new Date().getFullYear();
  month: number = new Date().getMonth() + 1;

  constructor(periodMode: PeriodMode, year: number, month: number) {
    this.periodMode = periodMode;
    this.year = year;
    this.month = month;
  }
}
