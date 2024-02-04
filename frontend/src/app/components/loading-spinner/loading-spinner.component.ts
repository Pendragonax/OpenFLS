import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-loading-spinner',
  templateUrl: './loading-spinner.component.html',
  styleUrls: ['./loading-spinner.component.css']
})
export class LoadingSpinnerComponent implements OnInit {

  @Input() description$: ReplaySubject<string> = new ReplaySubject()

  description: string = "Lade"

  constructor() { }

  ngOnInit(): void {
    this.initialize()
  }

  initialize() {
    this.description$.subscribe({
      next: value => this.description = value
    })
  }

}
