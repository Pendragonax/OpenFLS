import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-assistance-plan-analysis',
  templateUrl: './assistance-plan-analysis.component.html',
  styleUrls: ['./assistance-plan-analysis.component.css']
})
export class AssistancePlanAnalysisComponent implements OnInit {

  @Input() id = 0

  constructor() { }

  ngOnInit(): void {
    this.loadAnalysis()
  }

  loadAnalysis() {
  }
}
