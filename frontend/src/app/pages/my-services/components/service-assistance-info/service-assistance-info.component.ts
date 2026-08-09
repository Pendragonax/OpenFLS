import {Component, Input} from '@angular/core';
import {
  AssistancePlanEvaluationLeftDto,
  AssistancePlanHourTypeEvaluationLeftDto
} from "../../../../shared/dtos/assistance-plan-evaluation-left.dto";
import {AssistancePlanHourMode} from "../../../../shared/dtos/assistance-plan-hour-mode.model";

@Component({
  selector: 'app-service-assistance-info',
  templateUrl: './service-assistance-info.component.html',
  styleUrls: ['./service-assistance-info.component.css'],
  standalone: false
})
export class ServiceAssistanceInfoComponent {
  readonly AssistancePlanHourMode = AssistancePlanHourMode;
  @Input() assistancePlanSelected = false;
  @Input() loading = false;
  @Input() evaluation: AssistancePlanEvaluationLeftDto | null = null;
  @Input() info: AssistancePlanHourTypeEvaluationLeftDto[] = [];

  // Tooltip texts can be adjusted freely.
  tooltipLeftThisWeek = 'Es wird nur die aktuelle komplette Woche betrachtet von Montag - Sonntag.';
  tooltipLeftThisMonth = 'Es wird nur der aktuelle komplette Monat betrachtet.';
  tooltipLeftThisYear = 'Es wird nur das aktuelle komplette Jahr betrachtet.';
  tooltipLeftComplete = 'Es wird die komplette Laufzeit des Hilfeplans betrachtet.';

  getHourModeLabel(): string {
    return this.evaluation?.hourMode === AssistancePlanHourMode.CORRIDOR ? 'Korridor' : 'Exakt';
  }

  getHourModeDetails(): string {
    if (this.evaluation?.hourMode !== AssistancePlanHourMode.CORRIDOR) {
      return 'Exakter Hilfeplan';
    }

    return `${this.formatHourValue(this.evaluation.approvedHoursFrom)} h - ${this.formatHourValue(this.evaluation.approvedHoursTo)} h`;
  }

  private formatHourValue(value: number): string {
    return value.toLocaleString('de-DE', {maximumFractionDigits: 2});
  }
}
