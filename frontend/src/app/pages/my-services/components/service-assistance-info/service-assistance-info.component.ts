import {Component, Input} from '@angular/core';
import {AssistancePlanHourTypeEvaluationLeftDto} from "../../../../shared/dtos/assistance-plan-evaluation-left.dto";

@Component({
  selector: 'app-service-assistance-info',
  templateUrl: './service-assistance-info.component.html',
  styleUrls: ['./service-assistance-info.component.css'],
  standalone: false
})
export class ServiceAssistanceInfoComponent {
  @Input() assistancePlanSelected = false;
  @Input() loading = false;
  @Input() info: AssistancePlanHourTypeEvaluationLeftDto[] = [];

  // Tooltip texts can be adjusted freely.
  tooltipLeftThisWeek = 'Es wird nur die aktuelle komplette Woche betrachtet von Montag - Sonntag.';
  tooltipLeftThisMonth = 'Es wird nur der aktuelle komplette Monat betrachtet.';
  tooltipLeftThisYear = 'Es wird nur das aktuelle komplette Jahr betrachtet.';
}
