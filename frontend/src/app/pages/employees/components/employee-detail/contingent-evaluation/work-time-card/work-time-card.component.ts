import {Component, Input, OnInit} from '@angular/core';
import {ContingentInformationDTO} from "../../../../../../shared/dtos/calendar-information-dto.model";

@Component({
    selector: 'app-work-time-card',
    templateUrl: './work-time-card.component.html',
    styleUrls: ['./work-time-card.component.css'],
    standalone: false
})
export class WorkTimeCardComponent implements OnInit {
  @Input() contingentInformation: ContingentInformationDTO = new ContingentInformationDTO();
  @Input() title: string = "";

  constructor() { }

  ngOnInit(): void {
  }

  get isNegativeDifference(): boolean {
    return this.contingentInformation.differenceHours < 0 || this.contingentInformation.differenceMinutes < 0;
  }

  get statusClass(): string {
    return this.isNegativeDifference ? 'work-time-card--bad' : 'work-time-card--ok';
  }

  get executedHoursLabel(): string {
    return this.formatHours(this.contingentInformation.executedHours * 60 + this.contingentInformation.executedMinutes);
  }

  get contingentHoursLabel(): string {
    return this.formatHours(this.contingentInformation.contingentHours * 60 + this.contingentInformation.contingentMinutes);
  }

  get differenceHoursLabel(): string {
    const minutes = this.contingentInformation.differenceHours * 60 + this.contingentInformation.differenceMinutes;
    const prefix = minutes < 0 ? '-' : '';
    return `${prefix}${this.formatHours(Math.abs(minutes))}`;
  }

  private formatHours(totalMinutes: number): string {
    const hours = totalMinutes / 60;
    const formatted = hours.toFixed(1);
    return formatted.endsWith('.0') ? formatted.slice(0, -2) : formatted;
  }
}
