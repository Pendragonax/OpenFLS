import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subject, Subscription, interval} from 'rxjs';
import {startWith, switchMap, takeUntil} from 'rxjs/operators';
import {BackupStatusService} from '../../shared/services/backup-status.service';
import {
  BackupConfigDto,
  BackupHistoryEntryDto,
  BackupOverall,
  BackupRunDto,
  BackupStatusDto
} from '../../shared/dtos/backup-status-dto.model';

interface ConfigRow {
  label: string;
  value: string;
}

interface FailureHint {
  title: string;
  body: string;
  commands: string[];
}

@Component({
  selector: 'app-backup',
  templateUrl: './backup.component.html',
  styleUrls: ['./backup.component.css'],
  standalone: false
})
export class BackupComponent implements OnInit, OnDestroy {
  private static readonly REFRESH_INTERVAL_MS = 60_000;
  private static readonly HISTORY_LIMIT = 100;

  status: BackupStatusDto | null = null;
  history: BackupHistoryEntryDto[] = [];
  loading = true;
  loadError = false;
  lastUpdated: Date | null = null;
  restoreHelpOpen = false;

  readonly historyColumns = ['timestamp', 'kind', 'outcome', 'details'];

  private readonly stop$ = new Subject<void>();
  private manualRefresh?: Subscription;

  constructor(private backupStatusService: BackupStatusService) {}

  ngOnInit(): void {
    interval(BackupComponent.REFRESH_INTERVAL_MS)
      .pipe(startWith(0), switchMap(() => this.backupStatusService.status()), takeUntil(this.stop$))
      .subscribe({
        next: status => this.applyStatus(status),
        error: () => this.applyError()
      });

    interval(BackupComponent.REFRESH_INTERVAL_MS)
      .pipe(startWith(0), switchMap(() => this.backupStatusService.history(BackupComponent.HISTORY_LIMIT)), takeUntil(this.stop$))
      .subscribe({
        next: entries => (this.history = entries),
        error: () => (this.history = [])
      });
  }

  ngOnDestroy(): void {
    this.stop$.next();
    this.stop$.complete();
    this.manualRefresh?.unsubscribe();
  }

  refresh(): void {
    this.loading = true;
    this.manualRefresh?.unsubscribe();
    this.manualRefresh = this.backupStatusService.status().subscribe({
      next: status => this.applyStatus(status),
      error: () => this.applyError()
    });
    this.backupStatusService.history(BackupComponent.HISTORY_LIMIT).subscribe({
      next: entries => (this.history = entries),
      error: () => (this.history = [])
    });
  }

  overallLabel(overall: BackupOverall | undefined): string {
    switch (overall) {
      case 'ok':
        return 'Backup aktuell';
      case 'overdue':
        return 'Backup überfällig';
      case 'failed':
        return 'Letztes Backup fehlgeschlagen';
      default:
        return 'Kein Backup-Status verfügbar';
    }
  }

  overallClass(overall: BackupOverall | undefined): string {
    switch (overall) {
      case 'ok':
        return 'state-ok';
      case 'overdue':
        return 'state-warn';
      case 'failed':
        return 'state-error';
      default:
        return 'state-unknown';
    }
  }

  outcomeLabel(outcome: string | null | undefined): string {
    switch (outcome) {
      case 'success':
        return 'Erfolgreich';
      case 'failure':
        return 'Fehlgeschlagen';
      case null:
      case undefined:
        return 'Unbekannt';
      default:
        return outcome;
    }
  }

  kindLabel(kind: string): string {
    return kind === 'restore_test' ? 'Restore-Test' : 'Backup';
  }

  formatBytes(bytes: number | null | undefined): string {
    if (bytes == null) {
      return '–';
    }
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    const units = ['KB', 'MB', 'GB', 'TB'];
    let value = bytes / 1024;
    let unitIndex = 0;
    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex++;
    }
    return `${value.toFixed(1).replace('.', ',')} ${units[unitIndex]}`;
  }

  formatDuration(seconds: number | null | undefined): string {
    if (seconds == null) {
      return '–';
    }
    if (seconds < 60) {
      return `${seconds} s`;
    }
    const minutes = Math.floor(seconds / 60);
    const rest = seconds % 60;
    return rest === 0 ? `${minutes} min` : `${minutes} min ${rest} s`;
  }

  /** Human interval like "alle 6 Stunden" / "alle 5 Minuten". */
  formatEvery(seconds: number | null | undefined): string {
    if (seconds == null || seconds <= 0) {
      return '–';
    }
    if (seconds % 3600 === 0) {
      const hours = seconds / 3600;
      return hours === 1 ? 'jede Stunde' : `alle ${hours} Stunden`;
    }
    if (seconds % 60 === 0) {
      const minutes = seconds / 60;
      return minutes === 1 ? 'jede Minute' : `alle ${minutes} Minuten`;
    }
    return `alle ${seconds} Sekunden`;
  }

  run(status: BackupStatusDto | null, key: 'lastBackup' | 'lastRestoreTest'): BackupRunDto | null {
    return status ? status[key] : null;
  }

  /** Actionable hint shown when the last backup failed - keyed by the job's reason. */
  failureHint(): FailureHint | null {
    if (this.status?.overall !== 'failed') {
      return null;
    }
    const createUser = 'MYSQL_BACKUP_PASSWORD_FILE=secrets/db_backup_password.secret scripts/database_create_backup_user.sh';
    const restartBackup = 'docker compose -f docker/docker-compose.yml restart backup';
    const logs = 'docker compose -f docker/docker-compose.yml logs backup';
    switch (this.status?.lastBackup?.reason) {
      case 'backup_user_missing':
        return {
          title: 'Backup-Datenbankbenutzer nicht eingerichtet',
          body: 'Der Backup-Dienst kann sich nicht an der Datenbank anmelden. Der Benutzer „openfls_backup" fehlt oder das Passwort passt nicht. Einmalig auf dem Server ausführen:',
          commands: [createUser, restartBackup]
        };
      case 'backup_secret_missing':
        return {
          title: 'Backup-Passwort fehlt',
          body: 'Die Datei secrets/db_backup_password.secret ist nicht vorhanden oder nicht lesbar. Auf dem Server anlegen und danach den Benutzer einrichten:',
          commands: ['scripts/database_create_secrets.sh', createUser, restartBackup]
        };
      case 'insufficient_grants':
        return {
          title: 'Backup-Benutzer hat zu wenige Rechte',
          body: '„openfls_backup" fehlen Leserechte (SELECT, SHOW VIEW, TRIGGER) auf das Schema. Das Einrichtungsskript erneut ausführen:',
          commands: [createUser, restartBackup]
        };
      case 'database_unreachable':
        return {
          title: 'Datenbank nicht erreichbar',
          body: 'Der Backup-Dienst erreicht die Datenbank nicht. Prüfen, ob der db-Container läuft und beide im selben Netzwerk sind:',
          commands: ['docker compose -f docker/docker-compose.yml ps db', logs]
        };
      default:
        return {
          title: 'Letztes Backup fehlgeschlagen',
          body: 'Die Ursache ist nicht eindeutig klassifiziert. Häufigste Ursache: der '
            + 'Backup-Datenbankbenutzer „openfls_backup" wurde nicht angelegt. Prüfen und '
            + 'ggf. einrichten, sonst die Container-Logs ansehen:',
          commands: [createUser, restartBackup, logs]
        };
    }
  }

  nextExpectedBackup(): Date | null {
    const timestamp = this.status?.lastBackup?.timestamp;
    const intervalSeconds = this.status?.config?.intervalSeconds;
    if (!timestamp || !intervalSeconds) {
      return null;
    }
    const base = new Date(timestamp).getTime();
    if (Number.isNaN(base)) {
      return null;
    }
    return new Date(base + intervalSeconds * 1000);
  }

  configRows(config: BackupConfigDto | null | undefined): ConfigRow[] {
    if (!config) {
      return [];
    }
    return [
      {label: 'Datenbank', value: config.database ?? '–'},
      {label: 'Intervall', value: this.formatEvery(config.intervalSeconds)},
      {label: 'Wiederholung nach Fehler', value: this.formatEvery(config.retryIntervalSeconds)},
      {label: 'Lokale Aufbewahrung', value: config.retentionDays != null ? `${config.retentionDays} Tage` : '–'},
      {label: 'Verlauf (max. Einträge)', value: config.historyMaxEntries != null ? String(config.historyMaxEntries) : '–'},
      {label: 'Als überfällig ab', value: config.maxAgeHours != null ? `${config.maxAgeHours} Stunden` : '–'},
      {label: 'Sperre verwaist ab', value: this.formatEvery(config.staleLockSeconds)}
    ];
  }

  private applyStatus(status: BackupStatusDto): void {
    this.status = status;
    this.loading = false;
    this.loadError = false;
    this.lastUpdated = new Date();
  }

  private applyError(): void {
    this.loading = false;
    this.loadError = true;
  }
}
