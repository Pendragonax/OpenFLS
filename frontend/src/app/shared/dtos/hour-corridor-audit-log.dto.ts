export type HourCorridorAuditAction = 'CREATE' | 'UPDATE' | 'DELETE';

export interface HourCorridorAuditLogDto {
  id: number;
  hourCorridorId: number;
  action: HourCorridorAuditAction;
  changedAt: string;
  actor: string;
  beforeTitle: string | null;
  afterTitle: string | null;
  beforeWeeklyMinutesFrom: number | null;
  afterWeeklyMinutesFrom: number | null;
  beforeWeeklyMinutesTill: number | null;
  afterWeeklyMinutesTill: number | null;
  beforeHourTypeId: number | null;
  afterHourTypeId: number | null;
}
