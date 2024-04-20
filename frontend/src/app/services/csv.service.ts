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

  exportToCsvWithHeader(filename: string, rows: any[], header: string[]) {
    let clonedRows = [...rows]
    clonedRows.unshift(header)
    this.exportToCsv(filename, clonedRows);
  }

  exportToCsv(filename: string, rows: any[]) {
    if (!rows || !rows.length) {
      return;
    }

    const keys = Object.keys(rows[0]);
    const csvData = [
      ...this.convertRowsToCsv(rows, keys)
    ].join('\n');

    this.exportFile([csvData], 'text/csv;charset=utf-8;', filename);
  }

  exportFile(data: any, fileType: string, fileName: string) {
    const blob = new Blob(["\uFEFF"+data], { type: fileType });
    FileSaver.saveAs(blob, fileName);
  }

  private convertRowsToCsv(rows, keys) {
    return rows.map(row => {
      return keys.map(k => this.formatCell(row[k])).join(this.separator);
    });
  }

  private formatCell(cell) {
    if (cell == null) {
      return '';
    }

    if (cell instanceof Date) {
      cell = cell.toLocaleString();
    } else {
      cell = cell.toString().replace(/"/g, '""');
    }

    if (this.converter.isNumber(cell)) {
      cell = cell.toString().replace('.', this.decimalSeparator);
    }

    const regex = /("|;|,|\n)/g;
    if (cell.search(regex) >= 0) {
      cell = `"${cell}"`;
    }

    return cell;
  }
}
