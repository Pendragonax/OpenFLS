import {Component, Input, OnInit} from '@angular/core';
import {AssistancePlanService} from "../../../../services/assistance-plan.service";
import {AssistancePlanEvaluation} from "../../../../dtos/assistance-plan-evaluation.model";
import {delay} from "rxjs";

@Component({
  selector: 'app-assistance-plan-analysis',
  templateUrl: './assistance-plan-analysis.component.html',
  styleUrls: ['./assistance-plan-analysis.component.css']
})
export class AssistancePlanAnalysisComponent implements OnInit {

  @Input() id = 0

  eval: AssistancePlanEvaluation = new AssistancePlanEvaluation()

  // STATES
  isSubmitting = false;

  constructor(
    private assistancePlanService: AssistancePlanService
  ) { }

  ngOnInit(): void {
    this.isSubmitting = true
    this.loadAnalysis()
  }

  loadAnalysis() {
    this.assistancePlanService
      .getEvaluationById(this.id)
      .subscribe({
        next: (value) => {
          this.eval = value
          this.isSubmitting = false
        },
        error: () => this.isSubmitting = false,
        complete: () => this.isSubmitting = false
      })
  }
}
