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
  intervalSeconds: number | null;
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
