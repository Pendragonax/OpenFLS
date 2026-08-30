export interface LogEntryDto {
  timestamp: string;
  level: string;
  logger: string;
  thread: string;
  message: string;
  /** Multi-line exception stacktrace for this entry, or absent/null when there is none. */
  stacktrace?: string | null;
}

export interface LogSettingsDto {
  rootLevel: string;
  classLevels: {logger: string; level: string | null}[];
}
