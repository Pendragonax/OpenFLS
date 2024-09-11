import { Component, OnInit } from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ClientsService} from "../../shared/services/clients.service";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/services/comparer.helper";
import {ClientViewModel} from "../../shared/models/client-view.model";
import {combineLatest} from "rxjs";
import {UserService} from "../../shared/services/user.service";
import {TablePageComponent} from "../../shared/components/table-page.component";
import {HelperService} from "../../shared/services/helper.service";
import {EmployeeViewModel} from "../../shared/models/employee-view.model";
import {ServiceService} from "../../shared/services/service.service";

@Component({
  selector: 'app-client',
  templateUrl: './client.component.html',
  styleUrls: ['./client.component.css']
})
export class ClientComponent extends TablePageComponent<ClientViewModel, ClientViewModel> implements OnInit {
  // VARs
  tableColumns = ['name', 'institution', 'actions'];

  deleteServiceCount: number = 0;

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private clientService: ClientsService,
    private serviceService: ServiceService,
    private userService: UserService,
    private comparer: Comparer) {
    super(modalService, helperService)
  }

  loadValues() {
    this.isSubmitting = true;

    combineLatest([
      this.clientService.allValues$,
      this.userService.user$
    ]).subscribe(([clients, user]) => {
      this.values = clients
        .map(client => <ClientViewModel> {
          dto: client,
          editable: user.permissions
            .filter(perm => perm.affiliated)
            .some(perm => perm.institutionId === client.institution.id)});
      this.values$.next(this.values);
      this.filteredTableData = this.values;
      this.isSubmitting = false;

      this.refreshTablePage();
    });
  }

  getNewValue(): ClientViewModel {
    return new ClientViewModel()
  }

  initFormSubscriptions() {
  }

  fillEditForm(value: ClientViewModel) {
    throw new Error('Method not implemented.');
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(value =>
      value.dto.firstName.toLowerCase().includes(this.searchString)
      || value.dto.lastName.toLowerCase().includes(this.searchString)
      || value.dto.institution.name.toLowerCase().includes(this.searchString));

    this.refreshTablePage();
  }

  create(value: ClientViewModel) {
    throw new Error('Method not implemented.');
  }

  update(value: ClientViewModel) {
    throw new Error('Method not implemented.');
  }

  delete(value: ClientViewModel) {
    if (this.isSubmitting || value == null) return;

    this.isSubmitting = true;

    this.clientService
      .delete(value.dto.id)
      .subscribe({
        next: () => this.handleSuccess("Klient gelöscht"),
        error: () => this.handleFailure("Fehler beim löschen")
      })
  }

  onSearchStringChanges(searchString: string) {
    this.searchString = searchString
    this.filterTableData()
  }

  override handleDeleteModalOpen(value: ClientViewModel) {
    this.serviceService.getCountByClientId(value.dto.id)
      .subscribe({
        next: (value) => this.deleteServiceCount = value
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
          return this.comparer.compare(a.dto.lastName, b.dto.lastName, isAsc);
        default:
          return 0;
      }
    });
  }
}
