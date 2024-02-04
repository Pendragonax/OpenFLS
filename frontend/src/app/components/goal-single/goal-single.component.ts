import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {GoalResponseDto} from "../../dtos/goal-response-dto.model";

@Component({
  selector: 'app-goal-single',
  templateUrl: './goal-single.component.html',
  styleUrls: ['./goal-single.component.css']
})
export class GoalSingleComponent implements OnInit {

  @Input() goal$: ReplaySubject<GoalResponseDto> = new ReplaySubject<GoalResponseDto>()
  @Input() goal: GoalResponseDto = new GoalResponseDto()

  constructor() { }

  ngOnInit(): void {
  }

}
