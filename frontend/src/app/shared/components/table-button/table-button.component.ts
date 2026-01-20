import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {TableButtonCell} from "./TableButtonCell";

@Component({
    selector: 'app-table-button',
    templateUrl: './table-button.component.html',
    styleUrls: ['./table-button.component.css'],
    standalone: false
})
export class TableButtonComponent implements OnInit {
  @Input() columns$: ReplaySubject<string[]> = new ReplaySubject<string[]>();
  @Input() data$: ReplaySubject<TableButtonCell[][]> = new ReplaySubject<TableButtonCell[][]>();
  @Input() columnFixedWidthFromIndex$: ReplaySubject<number> = new ReplaySubject<number>();
  @Input() boldColumnIndices$: ReplaySubject<number[]> = new ReplaySubject<number[]>();

  @Output() evaluationEvent: EventEmitter<any> = new EventEmitter<any>();

  columns: string[] = [];
  data: TableButtonCell[][] = [];
  columnFixedWidthFromIndex: number = 0;
  boldColumnIndices: number[] = []

  constructor() { }

  ngOnInit(): void {
    this.columns$.subscribe({
      next: (value) => this.columns = value
    });
    this.data$.subscribe({
      next: (value) => {
        this.data = value
      }
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
