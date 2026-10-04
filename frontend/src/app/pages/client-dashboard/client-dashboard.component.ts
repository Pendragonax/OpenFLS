import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatDialog} from '@angular/material/dialog';
import {ClientDashboardService} from '../../shared/services/client-dashboard.service';
import {ClientDashboardDto} from '../../shared/dtos/client-dashboard-dto.model';
import {ClientTaskDto} from '../../shared/dtos/client-task-dto.model';
import {HelperService} from '../../shared/services/helper.service';
import {UserService} from '../../shared/services/user.service';
import {ConfirmationModalComponent} from '../../shared/modals/confirmation-modal/confirmation-modal.component';

/**
 * One page overview of a client: the current assistance plan with its evaluation,
 * the latest entries and the open tasks. Every tile links into the detail view it
 * summarises.
 */
@Component({
  selector: 'app-client-dashboard',
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.css'],
  standalone: false
})
export class ClientDashboardComponent implements OnInit {

  private readonly destroyRef = inject(DestroyRef);

  dashboard: ClientDashboardDto | null = null;
  isLoading = false;
  loadFailed = false;
  currentEmployeeId = 0;
  private currentClientId = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private clientDashboardService: ClientDashboardService,
    private helperService: HelperService,
    private userService: UserService,
    private matDialog: MatDialog
  ) {
  }

  get clientId(): number {
    return this.currentClientId;
  }

  get clientName(): string {
    if (this.dashboard == null) {
      return '';
    }

    return `${this.dashboard.firstName} ${this.dashboard.lastName}`.trim();
  }

  /**
   * "Alle Einträge" preselects the client and, when a relevant assistance plan is
   * known, its period - so the click lands on the entries that actually belong to
   * that plan instead of an unfiltered list.
   */
  get allEntriesRouterLink(): unknown[] {
    const plan = this.dashboard?.currentAssistancePlan;
    if (plan == null) {
      return ['/services/all', 0, 0, 0, 0, this.clientId];
    }

    return ['/services/all', plan.start, plan.end, 0, 0, this.clientId];
  }

  ngOnInit(): void {
    // The route is reused when only the id changes, so react to the param itself.
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        this.currentClientId = Number(params.get('id') ?? 0);
        this.dashboard = null;
        this.loadDashboard();
      });

    this.userService.user$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(employee => this.currentEmployeeId = employee.id);
  }

  loadDashboard() {
    const clientId = this.clientId;
    if (clientId <= 0) {
      this.loadFailed = true;
      return;
    }

    this.isLoading = true;
    this.loadFailed = false;

    this.clientDashboardService.getDashboard(clientId).subscribe({
      next: (dashboard) => {
        this.dashboard = dashboard;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.loadFailed = true;
      }
    });
  }

  /** Adding a favourite is harmless to undo; removing one is not, so that path
   *  asks for confirmation first. */
  toggleFavorite() {
    if (this.dashboard == null) {
      return;
    }

    if (this.dashboard.favorite) {
      this.openFavoriteRemovalConfirmation(() => this.removeFavorite());
      return;
    }

    this.addFavorite();
  }

  private addFavorite() {
    if (this.dashboard == null) {
      return;
    }

    this.clientDashboardService.addFavorite(this.dashboard.clientId).subscribe({
      next: () => {
        if (this.dashboard != null) {
          this.dashboard.favorite = true;
          this.helperService.openSnackBar('Zu den Favoriten hinzugefügt');
        }
      },
      error: () => this.helperService.openSnackBar('Favorit konnte nicht geändert werden')
    });
  }

  private removeFavorite() {
    if (this.dashboard == null) {
      return;
    }

    this.clientDashboardService.deleteFavorite(this.dashboard.clientId).subscribe({
      next: () => {
        if (this.dashboard != null) {
          this.dashboard.favorite = false;
          this.helperService.openSnackBar('Aus den Favoriten entfernt');
        }
      },
      error: () => this.helperService.openSnackBar('Favorit konnte nicht geändert werden')
    });
  }

  private openFavoriteRemovalConfirmation(operation: () => void) {
    const dialogRef = this.matDialog.open(ConfirmationModalComponent);
    dialogRef.componentInstance.description = 'Wollen Sie diese Klient:in wirklich aus den Favoriten entfernen?';
    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        operation();
      }
    });
  }

  onTasksChanged(tasks: ClientTaskDto[]) {
    if (this.dashboard == null) {
      return;
    }

    this.dashboard.tasks = tasks;
    this.dashboard.openTaskCount = tasks.filter(task => !task.done).length;
  }

  openClientDetail() {
    this.router.navigate(['/clients/detail', this.clientId]).then();
  }
}
