import { Component, OnInit } from '@angular/core';
import {TablePageComponent} from "../../shared/modules/table-page.component";
import {HourTypeDto} from "../../dtos/hour-type-dto.model";
import {Sort} from "@angular/material/sort";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HourTypeService} from "../../services/hour-type.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Converter} from "../../shared/converter.helper";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-hour-type',
  templateUrl: './hour-type.component.html',
  styleUrls: ['./hour-type.component.css']
})
export class HourTypeComponent extends TablePageComponent<HourTypeDto, HourTypeDto> implements OnInit {
  tableColumns: string[] = ['title', 'price', 'actions']

  editForm = new FormGroup({
    title: new FormControl(
      "",
      { validators: Validators.compose([Validators.required, Validators.minLength(1)])}),
    price: new FormControl(0, { validators: Validators.compose([Validators.required])})
  })

  get titleControl() { return this.editForm.controls['title']; }

  get priceControl() { return this.editForm.controls['price']; }

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    public converter: Converter,
    private hourTypeService: HourTypeService
  ) {
    super(modalService, helperService);
  }

  initFormSubscriptions() {
    this.titleControl.valueChanges.subscribe(value => {
      if (this.editValue != null)
        this.editValue.title = value
    });
    this.priceControl.valueChanges.subscribe(value => {
      if (this.editValue != null)
        this.editValue.price = value
    });
  }

  fillEditForm(value: HourTypeDto) {
    this.titleControl.setValue(value.title);
    this.priceControl.setValue(value.price);
  }

  loadValues() {
    if (this.isSubmitting)
      return

    this.isSubmitting = true;

    this.hourTypeService.allValues$.subscribe({
      next: (values) => {
        this.values = values;
        this.values$.next(values);
        this.filteredTableData = values;
        this.isSubmitting = false;

        this.refreshTablePage();
      },
      error: () => this.handleFailure("Fehler beim laden")
    })
  }

  create(value: HourTypeDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.hourTypeService.create(value).subscribe({
      next: () => this.handleSuccess("Stundentyp erfolgreich gespeichert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  update(value: HourTypeDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.hourTypeService.update(value.id, value).subscribe({
      next: () => this.handleSuccess("Stundentyp erfolgreich geändert"),
      error: () => this.handleFailure("Fehler beim speichern")
    });
  }

  delete(value: HourTypeDto) {
    if (this.isSubmitting)
      return;

    this.isSubmitting = true;

    this.hourTypeService.delete(value.id).subscribe({
      next: () => this.handleSuccess("Stundentyp gelöscht"),
      error: () => this.handleFailure("Fehler beim löschen")
    });
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(value =>
      value.title.toLowerCase().includes(this.searchString.toLowerCase()));

    this.refreshTablePage();
  }

  getNewValue(): HourTypeDto {
    return new HourTypeDto();
  }

  sortData(sort: Sort) {
  }
}
