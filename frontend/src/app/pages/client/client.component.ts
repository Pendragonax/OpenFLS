import {Component, OnInit} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MatDialog} from "@angular/material/dialog";
import {ClientsService} from "../../shared/services/clients.service";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/services/comparer.helper";
import {ClientViewModel} from "../../shared/models/client-view.model";
import {combineLatest} from "rxjs";
import {UserService} from "../../shared/services/user.service";
import {TablePageComponent} from "../../shared/components/table-page.component";
import {HelperService} from "../../shared/services/helper.service";

import {ServiceService} from "../../shared/services/service.service";
import {ReadableInstitutionDto} from "../../shared/dtos/institution-readable-dto.model";
import {InstitutionService} from "../../shared/services/institution.service";
import {ClientDashboardService} from "../../shared/services/client-dashboard.service";
import {mapVisibleClients} from "./helpers/client-visibility.helper";
import {ConfirmationModalComponent} from "../../shared/modals/confirmation-modal/confirmation-modal.component";

@Component({
    selector: 'app-client',
    templateUrl: './client.component.html',
    styleUrls: ['./client.component.css'],
    standalone: false
})
export class ClientComponent extends TablePageComponent<ClientViewModel, ClientViewModel> implements OnInit {
  // VARs
  tableColumns = ['name', 'institution', 'actions'];

  deleteServiceCount: number = 0;
  showArchivedEntries = false;
  canToggleArchivedEntries = false;
  readableInstitutions: ReadableInstitutionDto[] = [];
  institutionId: number | null = null;
  selectedInstitution: ReadableInstitutionDto | null = null;

  constructor(
    private institutionService: InstitutionService,
    override modalService: NgbModal,
    override helperService: HelperService,
    private clientService: ClientsService,
    private serviceService: ServiceService,
    private userService: UserService,
    private clientDashboardService: ClientDashboardService,
    private matDialog: MatDialog,
    private comparer: Comparer) {
    super(modalService, helperService)
  }

  loadValues() {
    this.isSubmitting = true;

    combineLatest([
      this.clientService.getAll(),
      this.userService.user$,
      this.institutionService.getAllReadable(),
      this.clientDashboardService.getFavorites()
    ]).subscribe(([clients, user, readableInstitutions, favorites]) => {
      this.canToggleArchivedEntries = user.access?.role === 1 || user.permissions
        .filter(perm => perm.changeInstitution)
        .length > 0;
      if (this.readableInstitutions != null) {
        this.readableInstitutions = readableInstitutions
      }
      this.values = mapVisibleClients(
        clients, user, this.showArchivedEntries, new Set(favorites.map(favorite => favorite.clientId)));
      this.values$.next(this.values);
      this.filteredTableData = this.values;
      this.isSubmitting = false;

      this.refreshTablePage();
    });
  }

  loadClients() {
    this.isSubmitting = true;

    combineLatest([
      this.clientService.getAll(),
      this.userService.user$,
      this.clientDashboardService.getFavorites()
    ]).subscribe(([clients, user, favorites]) => {
      this.values = mapVisibleClients(
        clients, user, this.showArchivedEntries, new Set(favorites.map(favorite => favorite.clientId)));
      this.values = this.values.filter(value => (this.selectedInstitution != null && this.selectedInstitution.id == value.dto.institution.id) || this.selectedInstitution == null)
      this.values$.next(this.values);
      this.filteredTableData = this.values;
      this.isSubmitting = false;

      this.refreshTablePage();
    });
  }

  /** Adding a favourite is harmless to undo; removing one is not, so that path
   *  asks for confirmation first. */
  toggleFavorite(client: ClientViewModel, event: Event) {
    event.stopPropagation();
    event.preventDefault();

    if (client.favorite) {
      const dialogRef = this.matDialog.open(ConfirmationModalComponent);
      dialogRef.componentInstance.description =
        `Wollen Sie ${client.dto.firstName} ${client.dto.lastName} wirklich aus den Favoriten entfernen?`;
      dialogRef.afterClosed().subscribe(confirmed => {
        if (confirmed) {
          this.removeFavorite(client);
        }
      });
      return;
    }

    this.addFavorite(client);
  }

  private addFavorite(client: ClientViewModel) {
    this.clientDashboardService.addFavorite(client.dto.id).subscribe({
      next: () => {
        client.favorite = true;
        this.helperService.openSnackBar('Zu den Favoriten hinzugefügt');
      },
      error: () => this.helperService.openSnackBar('Favorit konnte nicht geändert werden')
    });
  }

  private removeFavorite(client: ClientViewModel) {
    this.clientDashboardService.deleteFavorite(client.dto.id).subscribe({
      next: () => {
        client.favorite = false;
        this.helperService.openSnackBar('Aus den Favoriten entfernt');
      },
      error: () => this.helperService.openSnackBar('Favorit konnte nicht geändert werden')
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

  onInstitutionChanged(institution: ReadableInstitutionDto | null) {
    this.selectedInstitution = institution;
    this.institutionId = institution?.id ?? null;
    this.loadClients();
  }

  onArchivedVisibilityChanged(showArchivedEntries: boolean) {
    this.showArchivedEntries = showArchivedEntries;
    this.loadClients();
  }

  override handleDeleteModalOpen(value: ClientViewModel) {
    this.deleteServiceCount = 0;
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
