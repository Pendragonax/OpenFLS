import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-work-time-card',
  templateUrl: './work-time-card.component.html',
  styleUrls: ['./work-time-card.component.css']
})
export class WorkTimeCardComponent implements OnInit {
  @Input() time: [number, number, number, number] = [0, 0, 0, 0];
  @Input() title: string = "";

  constructor() { }

  ngOnInit(): void {
  }
}
