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
}
