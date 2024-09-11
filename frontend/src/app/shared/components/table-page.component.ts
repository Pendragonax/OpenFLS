import { Component, OnInit } from '@angular/core';
import {Subject} from "rxjs";
import {MatTableDataSource} from "@angular/material/table";
import {UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Sort} from "@angular/material/sort";
import {HelperService} from "../services/helper.service";
import {PageEvent} from "@angular/material/paginator";

@Component({
  selector: 'app-table-page',
  template: ``,
  styleUrls: []
})
export abstract class TablePageComponent<T, R> implements OnInit {

  // CONFIG
  abstract tableColumns: string[];

  // STATES
  isSubmitting = false;
  modalEditMode = false;

  // VARs
  values: T[] = [];
  values$ = new Subject<T[]>();
  editValue: T = this.getNewValue();
  tableSource: MatTableDataSource<R> = new MatTableDataSource();
  sourceTableData: R[] = [];
  filteredTableData: R[] = [];
  pageLength: number = 0;
  pageSize: number = 100;
  pageIndex: number = 0;

  // FILTER-VARs
  searchString: string = "";

  // FORMs
  filterForm = new UntypedFormGroup({
    searchString: new UntypedFormControl({value:"", disabled: this.isSubmitting})
  })

  protected constructor(
    protected modalService: NgbModal,
    protected helperService: HelperService
  ) { }

  get searchStringControl() { return this.filterForm.controls['searchString']; }

  ngOnInit(): void {
    this.initFilterFormSubscriptions();
    this.initFormSubscriptions();
    this.loadReferenceValues();
    this.loadValues();
  }

  loadReferenceValues() { }

  abstract loadValues();

  abstract create(value: T);

  abstract update(value: T);

  abstract delete(value: T);

  abstract getNewValue(): T;

  abstract filterTableData();

  abstract sortData(sort: Sort);

  abstract fillEditForm(value: T);

  abstract initFormSubscriptions();

  initFilterFormSubscriptions() {
    this.searchStringControl.valueChanges.subscribe((value) => {
      this.searchString = value.toLowerCase();
      this.filterTableData();
    });
  }

  resetFilterSearchString() {
    this.searchStringControl.setValue("");
  }

  refreshTablePage() {
    this.pageLength = this.filteredTableData.length;
    this.pageIndex = 0;
    this.setPage(this.pageIndex);
  }

  filterReferenceValues() { }

  handleInformationModalOpened() { }

  handleInformationModalClosed() { }

  handleEditModalClosed() { }

  handleDeleteModalOpen(value: T) { }

  handleDeleteModalClosed() { }

  handlePageEvent(e: PageEvent) {
    if (e != null) {
      this.pageIndex = e.pageIndex;
      this.pageSize = e.pageSize;

      this.setPage(e.pageIndex);
    }
  }

  setPage(pageIndex: number) {
    let targetLength = pageIndex * this.pageSize + this.pageSize;

    if (targetLength >= this.filteredTableData.length) {
      targetLength = this.filteredTableData.length;
    }

    this.tableSource.data = this.filteredTableData.slice(pageIndex * this.pageSize, targetLength);
  }

  openInformationModal(content, value: T) {
    this.editValue = value;
    this.handleInformationModalOpened();

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-info-title', scrollable: true })
      .result
      .then(() => this.handleInformationModalClosed())
      .catch(() => this.handleInformationModalClosed());
  }

  openEditModal(content, value: T | null) {
    if (value == null) {
      this.editValue = this.getNewValue();
      this.modalEditMode = false;
    }
    else {
      this.editValue = <T>{...value};
      this.modalEditMode = true;
    }

    this.fillEditForm(this.editValue);
    this.filterReferenceValues();

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-edit-title', scrollable: true })
      .result
      .then((result: Boolean) => {
        if (result && this.editValue != null) {
          if (value == null)
            this.create(this.editValue);
          else
            this.update(this.editValue);
        }
        this.handleEditModalClosed();
      })
      .catch(() => this.handleEditModalClosed());
  }

  openDeleteConfirmation(content, value: T) {
    this.handleDeleteModalOpen(value);

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-delete-title', scrollable: true })
      .result
      .then((result: Boolean) => {
        if (result)
          this.delete(value);
      })
      .catch(() => this.handleDeleteModalClosed());
  }

  handleSuccess(message: string) {
    this.isSubmitting = false;
    this.helperService.openSnackBar(message);

    this.loadValues();
  }

  handleFailure(message: string) {
    this.isSubmitting = false;
    this.helperService.openSnackBar(message);
  }
}
