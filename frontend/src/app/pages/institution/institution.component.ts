import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {InstitutionService} from "../../services/institution.service";
import {InstitutionDto} from "../../dtos/institution-dto.model";
import {UserService} from "../../services/user.service";
import {combineLatest} from "rxjs";
import {InstitutionView} from "../../models/institution-view.model";
import {EmployeeDto} from "../../dtos/employee-dto.model";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/comparer.helper";
import {TablePageComponent} from "../../shared/modules/table-page.component";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-institution',
  templateUrl: './institution.component.html',
  styleUrls: ['./institution.component.css']
})
export class InstitutionComponent extends TablePageComponent<InstitutionView, InstitutionView> implements OnInit {
  // VARs
  tableColumns = ['name', 'phone', 'email', 'actions'];

  constructor(
    private institutionService: InstitutionService,
    private router: Router,
    override modalService: NgbModal,
    override helperService: HelperService,
    private userService: UserService,
    private comparer: Comparer
  ) {
    super(modalService, helperService);
  }

  loadValues() {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    combineLatest([
      this.userService.user$,
      this.userService.leadingInstitutions$,
      this.institutionService.allValues$
    ]).subscribe(([user, leadingIds, institutions]) => {
      const isAdmin = this.isAdmin(user);
      this.values = institutions.map(value => <InstitutionView> {
        dto: value,
        editable: this.isEditable(leadingIds, value) || isAdmin});
      this.values$.next(this.values);
      this.filteredTableData = this.values;
      this.tableSource.data = this.values;
      this.isSubmitting = false;

      this.refreshTablePage();
    })
  }

  getNewValue(): InstitutionView {
    return new InstitutionView()
  }

  initFormSubscriptions() {
  }

  fillEditForm(value: InstitutionView) {
    throw new Error('Method not implemented.');
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(institution =>
      institution.dto.name.toLowerCase().includes(this.searchString.toLowerCase()));

    this.refreshTablePage();
  }

  create(value: InstitutionView) {
    throw new Error('Method not implemented.');
  }

  update(value: InstitutionView) {
    throw new Error('Method not implemented.');
  }

  delete(value: InstitutionView) {
    this.isSubmitting = true;

    this.institutionService
      .delete(value.dto.id)
      .subscribe({
        next: () => this.handleSuccess("Bereich gelöscht"),
        error: () => this.handleFailure("Fehler beim löschen")
      });
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
          return this.comparer.compare(a.dto.name, b.dto.name, isAsc);
        case this.tableColumns[1]:
          return this.comparer.compare(a.dto.phonenumber, b.dto.phonenumber, isAsc);
        case this.tableColumns[2]:
          return this.comparer.compare(a.dto.email, b.dto.email, isAsc);
        default:
          return 0;
      }
    });
  }

  private isAdmin(employee: EmployeeDto): boolean {
    return (employee.access?.role ?? 99) <= 1 ?? false
  }

  private isEditable(leadingIds: number[], institution: InstitutionDto | null): boolean {
    return leadingIds.some(id => id === institution?.id) ?? false;
  }
}
