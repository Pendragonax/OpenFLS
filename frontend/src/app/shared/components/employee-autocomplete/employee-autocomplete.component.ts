import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {AsyncPipe, NgForOf, NgIf} from "@angular/common";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatOptionModule} from "@angular/material/core";
import {FormControl, ReactiveFormsModule} from "@angular/forms";
import {Observable} from "rxjs";
import {map, startWith} from "rxjs/operators";
import {EmployeeSolo} from "../../dtos/employee-solo.projection";

@Component({
    selector: 'app-employee-autocomplete',
    imports: [
        AsyncPipe,
        MatAutocompleteModule,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatOptionModule,
        NgForOf,
        NgIf,
        ReactiveFormsModule
    ],
    templateUrl: './employee-autocomplete.component.html',
    styleUrl: './employee-autocomplete.component.css'
})
export class EmployeeAutocompleteComponent implements OnInit {

  @Input() employees: EmployeeSolo[] = [];
  @Input() employeeId: number | null = null;
  @Input() disabled: boolean = false;

  @Output() employeeChanged: EventEmitter<EmployeeSolo | null> = new EventEmitter<EmployeeSolo | null>();

  employee: EmployeeSolo | null = null;
  employeeControl: FormControl;
  filteredEmployees$!: Observable<EmployeeSolo[]>;

  constructor() {
    this.employeeControl = new FormControl({value: this.employee, disabled: this.disabled})
  }

  ngOnInit() {
    this.filteredEmployees$ = this.employeeControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value ?? "")),
    );

    this.employeeControl.valueChanges.subscribe(value => {
      if (isEmployeeSolo(value)) {
        this.employee = value;
        this.employeeChanged.emit(value);
      } else if (value == null) {
        this.employeeChanged.emit(value);
      }
    })
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['disabled']) {
      const disabled = changes['disabled'].currentValue;
      if (disabled) {
        this.employeeControl.disable({ emitEvent: false });
      } else {
        this.employeeControl.enable({ emitEvent: false });
      }
    }

    if (changes['employees']) {
      if (this.employeeId !== null && this.employees != null) {
        this.employee = this.employees.find(it => it.id == this.employeeId) ?? null;
        this.employeeControl.setValue(this.employee);
      }
    }
  }

  reset(event: MouseEvent) {
    event.stopPropagation();
    this.employeeControl.reset();
  }

  displayFn(employee: any): string {
    if (isEmployeeSolo(employee)) {
      return employee ? getFullName(employee) : '';
    }

    return '';
  }

  private _filter(value: any): EmployeeSolo[] {
    if (typeof value !== 'string') {
      return [];
    }

    const filterValue = value.toLowerCase();
    return this.employees.filter(option =>
      getFullName(option).toLowerCase().includes(filterValue.toLowerCase())
    );
  }

  protected readonly getFullName = getFullName;
}

function isEmployeeSolo(employee: any): employee is EmployeeSolo {
  return employee && typeof employee === 'object' && 'firstname' in employee && 'lastname' in employee;
}

function getFullName(employee: EmployeeSolo): string {
  return employee.lastname + " " + employee.firstname;
}
