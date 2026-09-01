export class ClientTaskDto {
  id: number = 0;
  clientId: number = 0;
  title: string = '';
  description: string = '';
  dueDate: string = '';
  createdAt: string = '';
  createdById: number | null = null;
  createdByName: string = '';
  done: boolean = false;
  overdue: boolean = false;
  completedById: number | null = null;
  completedByName: string | null = null;
  completedOn: string | null = null;
  completedAt: string | null = null;
  completionComment: string | null = null;
}

export interface CreateClientTaskDto {
  clientId: number;
  title: string;
  description: string;
  dueDate: string;
}

export interface UpdateClientTaskDto {
  id: number;
  title: string;
  description: string;
  dueDate: string;
}

export interface CompleteClientTaskDto {
  comment: string;
  completedOn: string;
}

export type ClientTaskAuditAction = 'CREATE' | 'UPDATE' | 'COMPLETE' | 'REOPEN' | 'DELETE';

export interface ClientTaskAuditLogDto {
  id: number;
  clientTaskId: number;
  action: ClientTaskAuditAction;
  changedAt: string;
  actor: string;
  beforeTitle: string | null;
  afterTitle: string | null;
  beforeDueDate: string | null;
  afterDueDate: string | null;
  beforeDone: boolean | null;
  afterDone: boolean | null;
  comment: string | null;
}
