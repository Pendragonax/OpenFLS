import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {AssistancePlanService} from "../../services/assistance-plan.service";

@Component({
  selector: 'app-goal-evaluation',
  templateUrl: './goal-evaluation.component.html',
  styleUrls: ['./goal-evaluation.component.css']
})
export class GoalEvaluationComponent implements OnInit {

  // CONST
  private validTabIndices = [0,1,2];
  private tabParamName = 'tab';
  private idParamName = 'id';

  // VAR
  assistancePlanId = 0;
  tabIndex = 0;

  constructor(private assistancePlanService: AssistancePlanService,
              private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.executeURLParams();
    this.assistancePlanService.getById(this.assistancePlanId).subscribe({
      next: value => console.log(value)
    })
  }

  executeURLParams() {
    this.route.params.subscribe(params => {
      // tab
      if (params[this.tabParamName]) {
        const urlTabIndex = +params[this.tabParamName]
        if (this.validTabIndices.includes(urlTabIndex, 0)) {
          this.tabIndex = urlTabIndex;
        }
      }

      // goalId
      if (params[this.idParamName]) {
        this.assistancePlanId = +params[this.idParamName];
      }
    });
  }

}
