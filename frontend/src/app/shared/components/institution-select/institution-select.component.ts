import {ChangeDetectorRef, Component, EventEmitter, Input, Output, SimpleChanges} from '@angular/core';
import {AsyncPipe, NgForOf, NgIf} from "@angular/common";
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {ReadableInstitutionDto} from "../../dtos/institution-readable-dto.model";

@Component({
    selector: 'app-institution-select',
    imports: [
        FormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatOptionModule,
        MatSelectModule,
        NgForOf,
        NgIf,
        ReactiveFormsModule
    ],
    templateUrl: './institution-select.component.html',
    styleUrl: './institution-select.component.css'
})
export class InstitutionSelectComponent {

  @Input() institutions: ReadableInstitutionDto[] = [];
  @Input() disabled: boolean = false;
  @Input() institutionId: number | null = null;

  @Output() institutionChanged: EventEmitter<ReadableInstitutionDto | null> = new EventEmitter<ReadableInstitutionDto | null>()

  institution: ReadableInstitutionDto | null = null;
  selectionGroup: FormGroup;

  get institutionControl() {
    return this.selectionGroup.controls['institutionControl']
  }

  constructor(private fb: FormBuilder,
              private cdr: ChangeDetectorRef) {
    this.selectionGroup = this.fb.group({
      institutionControl: new FormControl({value: '', disabled: this.disabled})
    });
  }

  ngOnInit() {
    this.initSubscription()
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['disabled']) {
      const disabled = changes['disabled'].currentValue;
      if (disabled) {
        this.institutionControl.disable({ emitEvent: false });
      } else {
        this.institutionControl.enable({ emitEvent: false });
      }
    }

    if (changes['institutions']) {
      if (this.institutionId !== null) {
        this.institution = this.institutions.find(it => it.id == this.institutionId) ?? null;
        this.institutionControl.setValue(this.institution);
      }
    }
  }

  initSubscription() {
    this.institutionControl.valueChanges.subscribe(value => {
      if (value != undefined) {
        let selectedInstitution =
          this.institutions.find(it => it.id == value.id)
        this.institutionChanged.emit(selectedInstitution)
      }
    })
  }

  reset(event: MouseEvent) {
    event.stopPropagation();
    this.institutionControl.reset();
    this.institutionChanged.emit(null);
  }
}
