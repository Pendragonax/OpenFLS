import {Component, Input, OnInit, Output} from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-confirmation-modal',
  templateUrl: './confirmation-modal.component.html',
  styleUrls: ['./confirmation-modal.component.css']
})
export class ConfirmationModalComponent implements OnInit {
  readonly CONFIRM_BUTTON_DESCRIPTION: string = "Bestätigen"
  readonly ABORT_BUTTON_DESCRIPTION: string = "Abbrechen"

  @Input() title: string = "Bestätigung"
  @Input() description: string = "Wollen sie diese Aktion wirklich ausführen?"

  constructor(public dialogRef: MatDialogRef<ConfirmationModalComponent>) { }

  ngOnInit(): void {
  }

  onConfirm(): void {
    // Close the dialog, return true
    this.dialogRef.close(true);
  }

  onDismiss(): void {
    // Close the dialog, return false
    this.dialogRef.close(false);
  }

}
