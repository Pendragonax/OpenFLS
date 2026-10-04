import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ClientTaskDto} from '../../../../shared/dtos/client-task-dto.model';
import {ClientTaskService} from '../../../../shared/services/client-task.service';
import {HelperService} from '../../../../shared/services/helper.service';
import {Converter} from '../../../../shared/services/converter.helper';
import {ClientTaskCreateModalComponent} from './modals/client-task-create-modal/client-task-create-modal.component';
import {ClientTaskDetailModalComponent} from './modals/client-task-detail-modal/client-task-detail-modal.component';
import {ClientTaskEditModalComponent} from './modals/client-task-edit-modal/client-task-edit-modal.component';
import {PageEvent} from '@angular/material/paginator';

/**
 * Tasks of a client. Every employee may create a task and tick it off with a
 * comment and a date; the backend keeps the record of who did what and when.
 * The card itself only ever shows a short line per task - creating a task and
 * checking one off both happen in a small modal, opened by clicking the task.
 */
@Component({
  selector: 'app-client-dashboard-tasks-card',
  templateUrl: './client-dashboard-tasks-card.component.html',
  styleUrls: ['./client-dashboard-tasks-card.component.css'],
  standalone: false
})
export class ClientDashboardTasksCardComponent {

  @Input() clientId: number = 0;
  @Input() tasks: ClientTaskDto[] = [];
  @Input() currentEmployeeId: number = 0;
  @Output() tasksChanged = new EventEmitter<ClientTaskDto[]>();

  showDoneTasks = false;
  completedTasks: ClientTaskDto[] = [];
  completedTotal = 0;
  completedPage = 0;

  constructor(
    private matDialog: MatDialog,
    private clientTaskService: ClientTaskService,
    private helperService: HelperService,
    private converter: Converter
  ) {
  }

  get openTasks(): ClientTaskDto[] {
    return this.tasks.filter(task => !task.done);
  }

  get doneTasks(): ClientTaskDto[] {
    return this.completedTasks;
  }

  get visibleTasks(): ClientTaskDto[] {
    return this.showDoneTasks ? this.doneTasks : this.openTasks;
  }

  toggleDoneTasks() {
    this.showDoneTasks = !this.showDoneTasks;
    if (this.showDoneTasks) this.loadCompleted(0);
  }

  onCompletedPage(event: PageEvent) { this.loadCompleted(event.pageIndex); }

  openCreateModal() {
    const dialogRef = this.matDialog.open(ClientTaskCreateModalComponent);
    dialogRef.componentInstance.clientId = this.clientId;

    dialogRef.afterClosed().subscribe(task => {
      if (task) {
        this.reload();
      }
    });
  }

  /** Opens the view + check-off modal; the description and, once done, the
   *  completion comment are only ever shown here. */
  openDetailModal(task: ClientTaskDto) {
    const dialogRef = this.matDialog.open(ClientTaskDetailModalComponent);
    dialogRef.componentInstance.initialize(task);

    dialogRef.afterClosed().subscribe(updatedTask => {
      if (updatedTask) {
        this.reload();
      }
    });
  }

  openEditModal(task: ClientTaskDto, event: Event) {
    event.stopPropagation();
    const dialogRef = this.matDialog.open(ClientTaskEditModalComponent);
    dialogRef.componentInstance.initialize(task);
    dialogRef.afterClosed().subscribe(updated => { if (updated) this.reload(); });
  }

  getDateString(value: string | null): string {
    return this.converter.getLocalDateString(value);
  }

  /** Subtle cue for tasks created by the person currently looking at the dashboard. */
  isOwnTask(task: ClientTaskDto): boolean {
    return this.currentEmployeeId > 0 && task.createdById === this.currentEmployeeId;
  }

  private reload() {
    this.clientTaskService.getByClientId(this.clientId).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.tasksChanged.emit(tasks);
        if (this.showDoneTasks) this.loadCompleted(this.completedPage);
      },
      error: () => this.helperService.openSnackBar('Aufgaben konnten nicht geladen werden')
    });
  }

  private loadCompleted(page: number) {
    this.clientTaskService.getCompletedByClientId(this.clientId, page).subscribe({
      next: result => {
        if (result.content.length === 0 && result.totalElements > 0 && page > 0) {
          this.loadCompleted(page - 1);
          return;
        }
        this.completedTasks = result.content;
        this.completedTotal = result.totalElements;
        this.completedPage = result.page;
      },
      error: () => this.helperService.openSnackBar('Erledigte Aufgaben konnten nicht geladen werden')
    });
  }
}
