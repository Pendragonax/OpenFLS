import {Component, EventEmitter, Input, Output, SimpleChanges} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule, UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-search-field',
  standalone: true,
    imports: [
        FormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        ReactiveFormsModule
    ],
  templateUrl: './search-field.component.html',
  styleUrl: './search-field.component.css'
})
export class SearchFieldComponent {

  @Input() label: string = "Suchtext"
  @Input() onResetClick!: () => void
  @Input() placeholder: string = "..."
  @Input() isSubmitting: boolean = false

  @Output() onSearchStringChanges: EventEmitter<string> = new EventEmitter<string>()

  // FORMs
  searchGroup = new UntypedFormGroup({
    searchControl: new UntypedFormControl({value:"", disabled: this.isSubmitting})
  })

  getSearchControl() { return this.searchGroup.controls['searchControl'] }

  ngOnInit(): void {
    this.getSearchControl().valueChanges.subscribe((value) => {
      this.onSearchStringChanges.emit(value)
      console.log(value)
    })
  }

  clickReset() {
    this.getSearchControl().setValue("")
  }

}
