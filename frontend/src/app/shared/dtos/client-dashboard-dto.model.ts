import {AssistancePlanPreviewDto} from "./assistance-plan-preview-dto.model";
import {ClientTaskDto} from "./client-task-dto.model";

export type ClientDashboardAccess = 'GRANTED' | 'DENIED';

export interface ClientLatestServiceDto {
  id: number;
  start: string;
  end: string;
  minutes: number;
  title: string;
  content: string;
  institutionId: number;
  institutionName: string;
  employeeId: number;
  employeeFirstname: string;
  employeeLastname: string;
  assistancePlanId: number;
}

export interface ClientDashboardDto {
  clientId: number;
  firstName: string;
  lastName: string;
  archived: boolean;
  institutionId: number;
  institutionName: string;
  favorite: boolean;
  canModifyClient: boolean;
  canWriteEntries: boolean;

  assistancePlanAccess: ClientDashboardAccess;
  currentAssistancePlan: AssistancePlanPreviewDto | null;
  assistancePlanCount: number;

  servicesAccess: ClientDashboardAccess;
  latestServices: ClientLatestServiceDto[];

  tasks: ClientTaskDto[];
  openTaskCount: number;
}

export interface ClientFavoriteDto {
  clientId: number;
  firstName: string;
  lastName: string;
  archived: boolean;
  institutionId: number;
  institutionName: string;
  hasActiveAssistancePlan: boolean;
  assistancePlanEnd: string | null;
  openTaskCount: number;
  overdueTaskCount: number;
}
