import {Component, OnInit} from '@angular/core';
import {EmployeeService} from "../../services/employee.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EmployeeDto} from "../../dtos/employee-dto.model";
import {UserService} from "../../services/user.service";
import {EmployeeView} from "../../models/employee-view.model";
import {combineLatest} from "rxjs";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/comparer.helper";
import {InstitutionService} from "../../services/institution.service";
import {TablePageComponent} from "../../shared/modules/table-page.component";
import {HelperService} from "../../services/helper.service";
import {ServiceService} from "../../services/service.service";

@Component({
  selector: 'app-employees',
  templateUrl: './employees.component.html',
  styleUrls: [
    './employees.component.css'
  ]
})
export class EmployeesComponent extends TablePageComponent<EmployeeView, EmployeeView> implements OnInit {
  // VARs
  tableColumns: string[] = ['roles', 'name', 'institution', 'actions'];

  deleteServiceCount: number = 0;

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private employeeService: EmployeeService,
    private userService: UserService,
    private serviceService: ServiceService,
    private comparer: Comparer,
    private institutionService: InstitutionService
  ) {
    super(modalService, helperService);
  }

  loadValues() {
    this.isSubmitting = true;

    combineLatest([
      this.userService.leadingInstitutions$,
      this.employeeService.allValues$,
      this.institutionService.allValues$,
      this.userService.isAdmin$]
    ).subscribe({
      next: ([leadingIds, employees, institutions, isAdmin]) => {
        this.values = employees.map(value => <EmployeeView>{
          dto: value,
          editable: isAdmin || this.isEmployeeEditable(leadingIds, value),
          administrator: (value?.access?.role ?? 99) <= 1 ?? false,
          leader: (value?.access?.role ?? 99) <= 2 ?? false,
          institutions: institutions
            .filter(institution =>
              value.permissions
                .some(permission => permission.affiliated && permission.institutionId == institution.id))
        });
        this.values$.next(this.values);
        this.filteredTableData = this.values;
        this.isSubmitting = false;

        this.refreshTablePage();
      },
      error: () => this.handleFailure("Fehler beim laden")
    });
  }

  getNewValue(): EmployeeView {
    return new EmployeeView()
  }

  initFormSubscriptions() {
  }

  fillEditForm(value: EmployeeView) {
    throw new Error('Method not implemented.');
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(employee =>
      employee.dto.firstName.toLowerCase().includes(this.searchString)
      || employee.dto.lastName.toLowerCase().includes(this.searchString)
      || employee.dto.description.toLowerCase().includes(this.searchString));

    this.refreshTablePage();
  }

  create(value: EmployeeView) {
    throw new Error('Method not implemented.');
  }

  update(value: EmployeeView) {
    throw new Error('Method not implemented.');
  }

  delete(employee: EmployeeView) {
    if (employee === null) return;

    this.isSubmitting = true;

    this.employeeService
      .delete(employee.dto.id)
      .subscribe({
        next: () => this.handleSuccess("Mitarbeiter gelöscht"),
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
        case this.tableColumns[1]:
          return this.comparer.compare(a.dto.lastName, b.dto.lastName, isAsc);
        default:
          return 0;
      }
    });
  }

  override handleDeleteModalOpen(value: EmployeeView) {
    this.serviceService.getCountByEmployeeId(value.dto.id)
      .subscribe({
        next: (value) => this.deleteServiceCount = value
      });
  }

  private isEmployeeEditable(leadingIds: number[], employee: EmployeeDto | null): boolean {
    return employee?.permissions
      ?.filter(perm => perm.affiliated)
      .some(perm => leadingIds.some(id => id === perm.institutionId)) ?? false;
  }
}
