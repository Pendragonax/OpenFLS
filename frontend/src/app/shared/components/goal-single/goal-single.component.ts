import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {GoalResponseDto} from "../../dtos/goal-response-dto.model";
import {Goal} from "../../projections/goal.projection";

@Component({
    selector: 'app-goal-single',
    templateUrl: './goal-single.component.html',
    styleUrls: ['./goal-single.component.css'],
    standalone: false
})
export class GoalSingleComponent implements OnInit {

  @Input() goal$: ReplaySubject<Goal> = new ReplaySubject<Goal>()
  @Input() goal: Goal = new Goal()

  constructor() { }

  ngOnInit(): void {
  }

  formatWeeklyMinutes(weeklyMinutes: number): number {
    const totalMinutes = Math.max(0, Math.round(Number(weeklyMinutes ?? 0)));
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return Number((hours + minutes / 100).toFixed(2));
  }

}
