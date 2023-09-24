import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-overview-table',
  templateUrl: './overview-table.component.html',
  styleUrls: ['./overview-table.component.css']
})
export class OverviewTableComponent implements OnInit {
  @Input() columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>();
  @Input() data$: ReplaySubject<string[][]> = new ReplaySubject<string[][]>();
  @Input() columnFixedWidthFromIndex$: ReplaySubject<number> = new ReplaySubject<number>();
  @Input() boldColumnIndices$: ReplaySubject<number[]> = new ReplaySubject<number[]>();

  columns: string[] = [];
  data: string[][] = [];
  columnFixedWidthFromIndex: number = 0;
  boldColumnIndices: number[] = []


  constructor() { }

  ngOnInit(): void {
    this.columns$.subscribe({
      next: (value) => this.columns = value
    });
    this.data$.subscribe({
      next: (value) => this.data = value
    });
    this.columnFixedWidthFromIndex$.subscribe({
      next: (value) => this.columnFixedWidthFromIndex = value
    });
    this.boldColumnIndices$.subscribe({
      next: (value) => this.boldColumnIndices = value
    });
  }

  arrayContainsNumber(value: number): boolean {
    return this.boldColumnIndices.includes(value)
  }
}
