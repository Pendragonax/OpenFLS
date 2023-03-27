import {Component, Input, OnInit} from '@angular/core';
import {AssistancePlanService} from "../../../services/assistance-plan.service";
import {AssistancePlanEvaluation} from "../../../dtos/assistance-plan-evaluation.model";

@Component({
  selector: 'app-assistance-plan-analysis',
  templateUrl: './assistance-plan-analysis.component.html',
  styleUrls: ['./assistance-plan-analysis.component.css']
})
export class AssistancePlanAnalysisComponent implements OnInit {

  @Input() id = 0

  eval: AssistancePlanEvaluation = new AssistancePlanEvaluation()

  constructor(
    private assistancePlanService: AssistancePlanService
  ) { }

  ngOnInit(): void {
    this.loadAnalysis()
  }

  loadAnalysis() {
    this.assistancePlanService.getEvaluationById(this.id).subscribe((value) => this.eval = value)
  }
}
