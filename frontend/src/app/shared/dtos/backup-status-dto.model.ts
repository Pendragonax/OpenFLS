export type BackupOverall = 'ok' | 'overdue' | 'failed' | 'unknown';

export type BackupFailureReason =
  | 'backup_user_missing'
  | 'database_unreachable'
  | 'insufficient_grants'
  | 'backup_secret_missing'
  | 'unknown';

export interface BackupRunDto {
  timestamp: string | null;
  outcome: string | null;
  message: string | null;
  backupFile: string | null;
  sizeBytes: number | null;
  sha256: string | null;
  durationSeconds: number | null;
  reason: string | null;
}

export interface BackupConfigDto {
  database: string | null;
  /** Local time of day the backup runs, `HH:MM`. */
  backupTime: string | null;
  /** IANA timezone `backupTime` is interpreted in. */
  timezone: string | null;
  /** Whole days between two backups (>= 1); 1 means daily. */
  intervalDays: number | null;
  retryIntervalSeconds: number | null;
  retentionDays: number | null;
  historyMaxEntries: number | null;
  maxAgeHours: number | null;
  staleLockSeconds: number | null;
  generatedAt: string | null;
}

export interface BackupStatusDto {
  lastBackup: BackupRunDto | null;
  lastRestoreTest: BackupRunDto | null;
  backupOverdue: boolean;
  maxAgeHours: number;
  overall: BackupOverall;
  config: BackupConfigDto | null;
  /** ISO-8601 instant of the next scheduled daily backup, or null. */
  nextExpectedBackup: string | null;
}

export interface BackupHistoryEntryDto {
  kind: 'backup' | 'restore_test';
  timestamp: string | null;
  outcome: string | null;
  message: string | null;
  backupFile: string | null;
  sizeBytes: number | null;
  sha256: string | null;
  durationSeconds: number | null;
}
