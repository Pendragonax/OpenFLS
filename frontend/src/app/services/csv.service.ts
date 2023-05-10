import { Injectable } from '@angular/core';
import {Converter} from "../shared/converter.helper";
import FileSaver from "file-saver";

@Injectable({
  providedIn: 'root'
})
export class CsvService {
  public separator = ';';
  public decimalSeparator = ',';

  constructor(
    private converter: Converter
  ) { }

  exportToCsv(filename: string, rows: object[]) {
    if (!rows || !rows.length) {
      return;
    }

    const keys = Object.keys(rows[0]);
    const csvData =
      rows.map(row => {
        return keys.map(k => {
          let cell = row[k] == null ? '' : row[k];

          cell = cell instanceof Date
            ? cell.toLocaleString()
            : cell.toString().replace(/"/g, '""');
          console.log(typeof cell)
          cell = this.converter.isNumber(cell)
            ? cell.toString().replace('.', this.decimalSeparator)
            : cell;

          const regex = /("|;|,|\n)/g;

          if (cell.search(regex) >= 0) {
            cell = `"${cell}"`;
          }
          return cell;
        }).join(this.separator);
      }).join('\n');

    this.exportFile([csvData], 'text/csv;charset=utf-8;', filename);
  }

  exportFile(data: any, fileType: string, fileName: string) {
    const blob = new Blob(["\uFEFF"+data], { type: fileType });
    FileSaver.saveAs(blob, fileName);
  }
}
