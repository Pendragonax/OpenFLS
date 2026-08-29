export interface LogEntryDto {
  timestamp: string;
  level: string;
  logger: string;
  thread: string;
  message: string;
}

export interface LogSettingsDto {
  rootLevel: string;
  classLevels: {logger: string; level: string | null}[];
}
