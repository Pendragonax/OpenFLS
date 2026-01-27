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
}
