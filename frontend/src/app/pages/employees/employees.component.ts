import {Component, OnInit} from '@angular/core';
import {EmployeeService} from "../../shared/services/employee.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EmployeeDto} from "../../shared/dtos/employee-dto.model";
import {UserService} from "../../shared/services/user.service";
import {EmployeeViewModel} from "../../shared/models/employee-view.model";
import {combineLatest} from "rxjs";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/services/comparer.helper";
import {InstitutionService} from "../../shared/services/institution.service";
import {TablePageComponent} from "../../shared/components/table-page.component";
import {HelperService} from "../../shared/services/helper.service";
import {ServiceService} from "../../shared/services/service.service";

@Component({
    selector: 'app-employees',
    templateUrl: './employees.component.html',
    styleUrls: [
        './employees.component.css'
    ],
    standalone: false
})
export class EmployeesComponent extends TablePageComponent<EmployeeViewModel, EmployeeViewModel> implements OnInit {
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
      this.institutionService.getAll(),
      this.userService.isAdmin$]
    ).subscribe({
      next: ([leadingIds, employees, institutions, isAdmin]) => {
        this.values = employees.map(value => <EmployeeViewModel>{
          dto: value,
          editable: isAdmin || this.isEmployeeEditable(leadingIds, value),
          administrator: (value?.access?.role ?? 99) <= 1,
          leader: (value?.access?.role ?? 99) <= 2,
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

  getNewValue(): EmployeeViewModel {
    return new EmployeeViewModel()
  }

  initFormSubscriptions() {
  }

  fillEditForm(value: EmployeeViewModel) {
    throw new Error('Method not implemented.');
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(employee =>
      employee.dto.firstName.toLowerCase().includes(this.searchString)
      || employee.dto.lastName.toLowerCase().includes(this.searchString)
      || employee.dto.description.toLowerCase().includes(this.searchString));

    this.refreshTablePage();
  }

  create(value: EmployeeViewModel) {
    throw new Error('Method not implemented.');
  }

  update(value: EmployeeViewModel) {
    throw new Error('Method not implemented.');
  }

  delete(employee: EmployeeViewModel) {
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

  onSearchStringChanges(searchString: string) {
    this.searchString = searchString
    this.filterTableData()
  }

  override handleDeleteModalOpen(value: EmployeeViewModel) {
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
