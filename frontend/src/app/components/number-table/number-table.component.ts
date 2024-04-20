import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {CsvService} from "../../services/csv.service";

@Component({
  selector: 'app-number-table',
  templateUrl: './number-table.component.html',
  styleUrls: ['./number-table.component.css']
})
export class NumberTableComponent implements OnInit {
  @Input() header$: ReplaySubject<string[]> = new ReplaySubject<string[]>()
  @Input() data$: ReplaySubject<any[][]> = new ReplaySubject<any[][]>()
  @Input() columnFixedWidthFromIndex: number = 0;
  @Input() boldColumnIndices: number[] = []
  @Input() exportFilename: string = "table_export"

  header: string[] = [];
  data: any[][] = [];

  constructor(private csvService: CsvService) { }

  ngOnInit(): void {
    this.header$.subscribe({
      next: (value) => this.header = value
    });
    this.data$.subscribe({
      next: (value) => {
        console.log(value)
        this.data = value}
    });
  }

  isNumberColumn(columnIndex: number): boolean {
    if (columnIndex >= this.data.length) {
      return false;
    }

    return this.data[columnIndex].every(value => typeof value === 'number')
  }

  boldIndicesContains(value: number): boolean {
    return this.boldColumnIndices.includes(value)
  }

  exportAsCSV() {
    this.csvService.exportToCsvWithHeader("export", this.data, this.header)
  }

}
