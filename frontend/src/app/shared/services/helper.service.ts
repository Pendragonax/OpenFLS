import { Injectable } from '@angular/core';
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class HelperService {

  constructor(
    private snackBar: MatSnackBar
  ) { }

  openSnackBar(text: string) {
    this.snackBar.open(
      text,
      "",
      { duration: 1500, verticalPosition: 'top', horizontalPosition: 'center' });
  }
}
