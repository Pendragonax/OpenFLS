import {ClientDto} from "../../../shared/dtos/client-dto.model";
import {EmployeeDto} from "../../../shared/dtos/employee-dto.model";
import {ClientViewModel} from "../../../shared/models/client-view.model";

export function mapVisibleClients(
  clients: ClientDto[],
  user: EmployeeDto,
  showArchivedEntries: boolean
): ClientViewModel[] {
  return clients
    .filter(client => showArchivedEntries || !client.archived)
    .map(client => <ClientViewModel> {
      dto: client,
      editable: user.permissions
        .filter(perm => perm.affiliated)
        .some(perm => perm.institutionId === client.institution.id)
    });
}
