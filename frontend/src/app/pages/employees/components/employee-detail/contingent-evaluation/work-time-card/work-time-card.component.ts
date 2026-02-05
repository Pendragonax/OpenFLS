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

  get isBad(): boolean {
    return this.contingentInformation.differenceHours < 0 || this.contingentInformation.differenceMinutes < 0;
  }

  get isWarning(): boolean {
    return this.contingentInformation.executedPercentage > this.contingentInformation.warningPercent && this.contingentInformation.executedPercentage < 100.0
  }

  get statusClass(): string {
    if (this.contingentInformation.executedPercentage < this.contingentInformation.warningPercent) {
      return 'work-time-card--bad';
    }

    if (this.contingentInformation.executedPercentage < 100.0) {
      return 'work-time-card--critical';
    }

    return 'work-time-card--ok';
  }
}
