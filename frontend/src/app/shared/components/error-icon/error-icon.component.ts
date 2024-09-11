import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-error-icon',
  templateUrl: './error-icon.component.html',
  styleUrls: ['./error-icon.component.css']
})
export class ErrorIconComponent implements OnInit {

  @Input() description: string = "Fehler"

  constructor() { }

  ngOnInit(): void {
  }

}
