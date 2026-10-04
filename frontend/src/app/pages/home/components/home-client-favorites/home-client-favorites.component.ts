import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ClientDashboardService} from '../../../../shared/services/client-dashboard.service';
import {ClientFavoriteDto} from '../../../../shared/dtos/client-dashboard-dto.model';
import {HelperService} from '../../../../shared/services/helper.service';
import {Converter} from '../../../../shared/services/converter.helper';
import {ConfirmationModalComponent} from '../../../../shared/modals/confirmation-modal/confirmation-modal.component';

/**
 * Favourite clients of the signed in employee. This is the entry point of the home
 * view: one click opens the dashboard of that client.
 */
@Component({
  selector: 'app-home-client-favorites',
  templateUrl: './home-client-favorites.component.html',
  styleUrls: ['./home-client-favorites.component.css'],
  standalone: false
})
export class HomeClientFavoritesComponent implements OnInit {

  favorites: ClientFavoriteDto[] = [];
  isLoading = false;
  loadFailed = false;
  searchString = '';

  constructor(
    private clientDashboardService: ClientDashboardService,
    private helperService: HelperService,
    private converter: Converter,
    private matDialog: MatDialog
  ) {
  }

  get filteredFavorites(): ClientFavoriteDto[] {
    const search = this.searchString.trim().toLowerCase();
    if (search.length === 0) {
      return this.favorites;
    }

    return this.favorites.filter(favorite =>
      favorite.firstName.toLowerCase().includes(search) ||
      favorite.lastName.toLowerCase().includes(search) ||
      favorite.institutionName.toLowerCase().includes(search));
  }

  ngOnInit(): void {
    this.loadFavorites();
  }

  loadFavorites() {
    this.isLoading = true;
    this.loadFailed = false;

    this.clientDashboardService.getFavorites().subscribe({
      next: (favorites) => {
        this.favorites = favorites;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.loadFailed = true;
      }
    });
  }

  removeFavorite(favorite: ClientFavoriteDto, event: Event) {
    event.stopPropagation();
    event.preventDefault();

    const dialogRef = this.matDialog.open(ConfirmationModalComponent);
    dialogRef.componentInstance.description =
      `Wollen Sie ${this.getFullName(favorite)} wirklich aus den Favoriten entfernen?`;
    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.deleteFavorite(favorite);
      }
    });
  }

  private deleteFavorite(favorite: ClientFavoriteDto) {
    this.clientDashboardService.deleteFavorite(favorite.clientId).subscribe({
      next: () => {
        this.favorites = this.favorites.filter(value => value.clientId !== favorite.clientId);
        this.helperService.openSnackBar('Aus den Favoriten entfernt');
      },
      error: () => this.helperService.openSnackBar('Favorit konnte nicht entfernt werden')
    });
  }

  onSearchStringChanges(value: string) {
    this.searchString = value;
  }

  getDateString(value: string | null): string {
    return this.converter.getLocalDateString(value);
  }

  getFullName(favorite: ClientFavoriteDto): string {
    return `${favorite.firstName} ${favorite.lastName}`.trim();
  }

  getTaskTooltip(favorite: ClientFavoriteDto): string {
    if (favorite.openTaskCount === 0) {
      return 'Keine offenen Aufgaben';
    }
    if (favorite.overdueTaskCount > 0) {
      return `${favorite.openTaskCount} offene Aufgaben, davon ${favorite.overdueTaskCount} überfällig`;
    }
    return `${favorite.openTaskCount} offene Aufgaben`;
  }
}
