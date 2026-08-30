import '@testbed';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CommonModule} from '@angular/common';
import {of} from 'rxjs';
import {BackupComponent} from './backup.component';
import {BackupStatusService} from '../../shared/services/backup-status.service';
import {BackupStatusDto} from '../../shared/dtos/backup-status-dto.model';

describe('BackupComponent', () => {
  let component: BackupComponent;
  let fixture: ComponentFixture<BackupComponent>;

  const status: BackupStatusDto = {
    lastBackup: {
      timestamp: '2026-08-30T02:00:00.000Z',
      outcome: 'success',
      message: 'ok',
      backupFile: '20260830T020000Z-openfls.sql.gz',
      sizeBytes: 1829417,
      sha256: 'abc123',
      durationSeconds: 13,
      reason: null
    },
    lastRestoreTest: null,
    backupOverdue: false,
    maxAgeHours: 7,
    overall: 'ok',
    config: {
      database: 'openfls',
      intervalSeconds: 21600,
      retryIntervalSeconds: 300,
      retentionDays: 14,
      historyMaxEntries: 1000,
      maxAgeHours: 7,
      staleLockSeconds: 43200,
      generatedAt: '2026-08-30T00:00:00.000Z'
    }
  };

  const serviceStub: Partial<BackupStatusService> = {
    status: () => of(status),
    history: () => of([
      {kind: 'backup', timestamp: '2026-08-30T02:00:00.000Z', outcome: 'success', message: 'ok', backupFile: 'x', sizeBytes: 1, sha256: 'h', durationSeconds: 1},
      {kind: 'backup', timestamp: '2026-08-29T02:00:00.000Z', outcome: 'failure', message: 'boom', backupFile: 'y', sizeBytes: 0, sha256: '', durationSeconds: 0}
    ])
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [BackupComponent],
      providers: [{provide: BackupStatusService, useValue: serviceStub}]
    }).compileComponents();

    fixture = TestBed.createComponent(BackupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load status, history and config', () => {
    expect(component).toBeTruthy();
    expect(component.status?.overall).toBe('ok');
    expect(component.history.length).toBe(2);
    expect(component.status?.config?.intervalSeconds).toBe(21600);
    expect(component.loading).toBe(false);
  });

  it('formatBytes renders a human readable size', () => {
    expect(component.formatBytes(1829417)).toBe('1,7 MB');
    expect(component.formatBytes(null)).toBe('–');
    expect(component.formatBytes(512)).toBe('512 B');
  });

  it('formatDuration renders seconds and minutes', () => {
    expect(component.formatDuration(13)).toBe('13 s');
    expect(component.formatDuration(65)).toBe('1 min 5 s');
    expect(component.formatDuration(120)).toBe('2 min');
    expect(component.formatDuration(null)).toBe('–');
  });

  it('formatEvery renders hours and minutes', () => {
    expect(component.formatEvery(21600)).toBe('alle 6 Stunden');
    expect(component.formatEvery(3600)).toBe('jede Stunde');
    expect(component.formatEvery(300)).toBe('alle 5 Minuten');
    expect(component.formatEvery(null)).toBe('–');
  });

  it('nextExpectedBackup adds the interval to the last backup timestamp', () => {
    const next = component.nextExpectedBackup();
    expect(next).not.toBeNull();
    expect(next!.toISOString()).toBe('2026-08-30T08:00:00.000Z');
  });

  it('configRows lists the effective configuration', () => {
    const rows = component.configRows(status.config);
    expect(rows.find(r => r.label === 'Lokale Aufbewahrung')?.value).toBe('14 Tage');
    expect(rows.find(r => r.label === 'Intervall')?.value).toBe('alle 6 Stunden');
  });

  it('failureHint is null while the backup is healthy', () => {
    expect(component.failureHint()).toBeNull();
  });

  it('failureHint explains a missing backup user with the setup command', () => {
    component.status = {
      ...status,
      overall: 'failed',
      lastBackup: {...status.lastBackup!, outcome: 'failure', reason: 'backup_user_missing'}
    };
    const hint = component.failureHint();
    expect(hint).not.toBeNull();
    expect(hint!.title).toContain('nicht eingerichtet');
    expect(hint!.commands.some(c => c.includes('database_create_backup_user.sh'))).toBe(true);
  });

  it('failureHint for an unknown reason still names the backup user and the logs', () => {
    component.status = {
      ...status,
      overall: 'failed',
      lastBackup: {...status.lastBackup!, outcome: 'failure', reason: 'unknown'}
    };
    const hint = component.failureHint()!;
    expect(hint.commands.some(c => c.includes('database_create_backup_user.sh'))).toBe(true);
    expect(hint.commands.some(c => c.includes('logs backup'))).toBe(true);
  });

  it('overallLabel maps every state', () => {
    expect(component.overallLabel('ok')).toContain('aktuell');
    expect(component.overallLabel('overdue')).toContain('überfällig');
    expect(component.overallLabel('failed')).toContain('fehlgeschlagen');
    expect(component.overallLabel('unknown')).toContain('Kein');
  });
});
