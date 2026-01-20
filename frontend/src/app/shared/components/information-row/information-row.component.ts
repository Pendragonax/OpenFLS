import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-information-row',
    templateUrl: './information-row.component.html',
    styleUrls: ['./information-row.component.css'],
    standalone: false
})
export class InformationRowComponent implements OnInit {

  @Input() title: string = "";
  @Input() content: string = "";

  constructor() { }

  ngOnInit(): void {
  }
}
