import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {CsvService} from "../../services/csv.service";

export interface ObjectTableRowColors {
  fontColor: string;
  backgroundColor: string;
}

@Component({
    selector: 'app-object-table',
    templateUrl: './object-table.component.html',
    styleUrls: ['./object-table.component.css'],
    standalone: false
})
export class ObjectTableComponent implements OnInit {
  @Input() header$: ReplaySubject<string[]> = new ReplaySubject<string[]>()
  @Input() data$: ReplaySubject<any[][]> = new ReplaySubject<any[][]>()
  @Input() columnFixedWidthFromIndex: number = 0;
  @Input() boldColumnIndices: number[] = []
  @Input() exportFilename: string = "table_export"
  @Input() rowClasses: string[] = []
  @Input() rowColors: Map<number, ObjectTableRowColors> = new Map()

  header: string[] = [];
  data: any[][] = [];

  constructor(private csvService: CsvService) { }

  ngOnInit(): void {
    this.header$.subscribe({
      next: (value) => this.header = value
    });
    this.data$.subscribe({
      next: (value) => this.data = value
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

  getRowColors(rowIndex: number): ObjectTableRowColors | null {
    return this.rowColors.get(rowIndex) ?? null;
  }

  exportAsCSV() {
    this.csvService.exportToCsvWithHeader("export", this.data, this.header)
  }

}
