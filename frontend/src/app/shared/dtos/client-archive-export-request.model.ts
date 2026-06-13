import {ClientArchiveExportFormat} from "./client-archive-export-format.model";

export class ClientArchiveExportRequest {
  format: ClientArchiveExportFormat = ClientArchiveExportFormat.JSON;
  anonymize: boolean = false;
}
