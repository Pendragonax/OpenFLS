import {Component, Inject, Input} from '@angular/core';
import {MatButtonModule} from "@angular/material/button";
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {NgForOf} from "@angular/common";

@Component({
    selector: 'app-information',
    imports: [
        MatButtonModule,
        MatDialogModule,
        NgForOf
    ],
    templateUrl: './information.modal.html',
    styleUrl: './information.modal.css'
})
export class InformationModal {

  constructor(@Inject(MAT_DIALOG_DATA) public data: { title: string, content: { title: string, content: string }[] },
              private dialogRef: MatDialogRef<InformationModal>) {
  }

  readonly CLOSE_BUTTON_DESCRIPTION: string = "Schließen"

  closeDialog(): void {
    this.dialogRef.close();
  }
}
