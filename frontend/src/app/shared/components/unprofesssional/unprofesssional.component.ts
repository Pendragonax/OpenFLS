import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UnprofessionalDto} from "../../dtos/unprofessional-dto.model";
import {Subject} from "rxjs";
import {MatTableDataSource} from "@angular/material/table";
import {combineLatest} from "rxjs";
import {EmployeeService} from "../../services/employee.service";
import {SponsorService} from "../../services/sponsor.service";
import {DtoCombinerService} from "../../services/dto-combiner.service";
import {EmployeeDto} from "../../dtos/employee-dto.model";
import {SponsorDto} from "../../dtos/sponsor-dto.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {Converter} from "../../services/converter.helper";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from "@angular/material/core";

@Component({
    selector: 'app-unprofesssional',
    templateUrl: './unprofesssional.component.html',
    styleUrls: ['./unprofesssional.component.css'],
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
        {
            provide: DateAdapter,
            useClass: NativeDateAdapter,
            deps: [MAT_DATE_LOCALE],
        },
        { provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS },
    ],
    standalone: false
})
export class UnprofesssionalComponent implements OnInit {

  @Input() values$: Subject<UnprofessionalDto[]> = new Subject<UnprofessionalDto[]>();
  @Input() hideAddButton: Boolean = false;
  @Output() addedValueEvent: EventEmitter<UnprofessionalDto> = new EventEmitter<UnprofessionalDto>();
  @Output() updatedValueEvent: EventEmitter<UnprofessionalDto> = new EventEmitter<UnprofessionalDto>();
  @Output() deletedValueEvent: EventEmitter<UnprofessionalDto> = new EventEmitter<UnprofessionalDto>();

  // VARs
  tableSource: MatTableDataSource<[EmployeeDto, SponsorDto, UnprofessionalDto]> = new MatTableDataSource();
  editValue: UnprofessionalDto = new UnprofessionalDto();
  sponsors: SponsorDto[] = [];

  // CONFIG
  displayedColumns: string[] = ['employee', 'sponsor', 'end', 'actions'];

  // FORMS
  editForm = new UntypedFormGroup({
    sponsor: new UntypedFormControl(null, Validators.compose([Validators.required])),
    end: new UntypedFormControl('', Validators.compose([Validators.required]))
  });

  get sponsorControl() {
    return this.editForm.controls['sponsor'];
  }

  get endControl() {
    return this.editForm.controls['end'];
  }

  constructor(
    private employeeService: EmployeeService,
    private sponsorService: SponsorService,
    private combiner: DtoCombinerService,
    private converter: Converter,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.loadValues();
  }

  delete(value: UnprofessionalDto) {
    this.deletedValueEvent.emit(value);
  }

  loadValues() {
    combineLatest([
      this.values$,
      this.sponsorService.allValues$,
      this.employeeService.allValues$
    ])
      .subscribe(([unprofessionals, sponsors, employees]) => {
        this.sponsors = sponsors.filter(sponsor => !unprofessionals.some(unprof => unprof.sponsorId == sponsor.id));
        this.tableSource.data = this.combiner.combineNotProfessionals(unprofessionals, employees, sponsors);
      });
  }

  setEnd(event) {
    if (event.value != null) {
      this.editValue.end = this.converter.formatDate(new Date(event.value.toString()))
      return;
    }

    this.editValue.end = null;
  }

  resetEnd() {
    this.editValue.end = null
    this.endControl?.setValue(null)
  }

  openValueModal(content, value: UnprofessionalDto | null = null) {
    if (value != null) {
      this.sponsorControl.disable();
      this.editValue = <UnprofessionalDto>{...value};
    }
    else {
      this.sponsorControl.enable();
      this.editValue = new UnprofessionalDto();
    }

    this.sponsorControl.setValue(this.editValue.sponsorId);
    if (this.editValue.end != null)
      this.endControl.setValue(new Date(this.editValue.end));

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-title', scrollable: true })
      .result
      .then((result: Boolean) => {
        if (result) {
          if (value == null) {
            this.editValue.sponsorId = this.sponsorControl.value;
            this.addedValueEvent.emit(this.editValue);
          } else {
            this.updatedValueEvent.emit(this.editValue);
          }
        }
      });
  }

  openDeleteModal(content, value: UnprofessionalDto) {
    this.modalService
      .open(content, {ariaLabelledBy: 'modal-basic-title-delete', scrollable: true})
      .result
      .then((result: Boolean) => {
        if (result) {
          this.delete(value);
        }
      });
  }

  getLocalDateString(date: string | null) : string {
    return this.converter.getLocalDateString(date);
  }
}
