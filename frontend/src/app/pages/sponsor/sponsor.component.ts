import { Component, OnInit } from '@angular/core';
import {SponsorDto} from "../../shared/dtos/sponsor-dto.model";
import {SponsorService} from "../../shared/services/sponsor.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/services/comparer.helper";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {TablePageComponent} from "../../shared/components/table-page.component";
import {HelperService} from "../../shared/services/helper.service";

@Component({
    selector: 'app-sponsor',
    templateUrl: './sponsor.component.html',
    styleUrls: ['./sponsor.component.css'],
    standalone: false
})
export class SponsorComponent extends TablePageComponent<SponsorDto, SponsorDto> implements OnInit {

  // VARs
  tableColumns = ['name', 'overhang', 'exact', 'actions'];
  sponsors: SponsorDto[] = [];

  editForm = new UntypedFormGroup({
    name: new UntypedFormControl({value: ''}, Validators.compose([
      Validators.required,
      Validators.minLength(1)
    ])),
    payOverhead: new UntypedFormControl(false),
    payExact: new UntypedFormControl(false)
  });

  get nameControl() { return this.editForm.controls['name']; }

  get payOverhangControl() { return this.editForm.controls['payOverhead']; }

  get payExactControl() { return this.editForm.controls['payExact']; }

  constructor(
    private sponsorService: SponsorService,
    override modalService: NgbModal,
    override helperService: HelperService,
    private comparer: Comparer
  ) {
    super(modalService, helperService);
  }

  loadValues() {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    this.sponsorService.allValues$.subscribe({
      next: (values) => {
        this.values = values;
        this.values$.next(values);
        this.filteredTableData = this.values;
        this.tableSource.data = this.filteredTableData;
        this.isSubmitting = false;

        this.refreshTablePage();
      },
      error: () => this.handleFailure("Fehler beim laden")
    })
  }

  getNewValue(): SponsorDto {
    return new SponsorDto()
  }

  initFormSubscriptions() {
    this.nameControl.valueChanges.subscribe(value => this.editValue.name = value);
    this.payOverhangControl.valueChanges.subscribe(value => this.editValue.payOverhang = value);
    this.payExactControl.valueChanges.subscribe(value => this.editValue.payExact = value);
  }

  create(value: SponsorDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.sponsorService.create(value).subscribe({
      next: () => this.handleSuccess("Kostenträger gespeichert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  update(value: SponsorDto) {
    if (this.isSubmitting || value.id <= 0)
      return;

    this.isSubmitting = true;

    this.sponsorService.update(value.id, value).subscribe({
      next: () => this.handleSuccess("Kostenträger geändert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  delete(value: SponsorDto) {
    if (this.isSubmitting || value.id <= 0)
      return;

    this.isSubmitting = true;

    this.sponsorService
      .delete(value.id)
      .subscribe({
        next: () => this.handleSuccess("Kostenträger gelöscht"),
        error: () => this.handleFailure("Fehler beim löschen")
      })
  }

  sortData(sort: Sort) {
    const data = this.tableSource.data.slice();
    if (!sort.active || sort.direction === '') {
      this.tableSource.data = data;
      return;
    }

    this.tableSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case this.tableColumns[0]:
          return this.comparer.compare(a.name, b.name, isAsc);
        default:
          return 0;
      }
    });
  }

  fillEditForm(value: SponsorDto) {
    this.nameControl.setValue(value.name);
    this.payExactControl.setValue(value.payExact);
    this.payOverhangControl.setValue(value.payOverhang);
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(value =>
      value.name.toLowerCase().includes(this.searchString.toLowerCase()));

    this.refreshTablePage();
  }

  onSearchStringChanges(searchString: string) {
    this.searchString = searchString
    this.filterTableData()
  }
}
