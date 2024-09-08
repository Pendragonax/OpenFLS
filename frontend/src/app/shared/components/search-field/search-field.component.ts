import {Component, EventEmitter, Input, Output, SimpleChanges} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule, UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {debounceTime, ReplaySubject} from "rxjs";
import {MatSelectModule} from "@angular/material/select";

@Component({
  selector: 'app-search-field',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
    MatSelectModule
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

  get searchControl() { return this.searchGroup.controls['searchControl'] }

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(debounceTime(500))
      .subscribe((value) => {
      this.onSearchStringChanges.emit(value)
    })
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isSubmitting']) {
      const disabled = changes['isSubmitting'].currentValue;
      if (disabled) {
        this.searchControl.disable({emitEvent: false});
      } else {
        this.searchControl.enable({emitEvent: false});
      }
    }
  }

  clickReset() {
    this.searchControl.setValue("")
  }

}
