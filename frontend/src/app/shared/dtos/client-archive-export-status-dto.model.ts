import {ClientArchiveExportFormat} from "./client-archive-export-format.model";
import {ClientArchiveExportDownloadLinkDto} from "./client-archive-export-download-link-dto.model";

export class ClientArchiveExportStatusDto {
  ready: boolean = false;
  format: ClientArchiveExportFormat = ClientArchiveExportFormat.JSON;
  requestedAt: string = '';
  requestedByEmployeeId: number = 0;
  downloadLink: ClientArchiveExportDownloadLinkDto | null = null;
}
