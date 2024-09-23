import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {GoalResponseDto} from "../../dtos/goal-response-dto.model";
import {Goal} from "../../projections/goal.projection";

@Component({
  selector: 'app-goal-single',
  templateUrl: './goal-single.component.html',
  styleUrls: ['./goal-single.component.css']
})
export class GoalSingleComponent implements OnInit {

  @Input() goal$: ReplaySubject<Goal> = new ReplaySubject<Goal>()
  @Input() goal: Goal = new Goal()

  constructor() { }

  ngOnInit(): void {
  }

}
