
import {ClientDto} from "../dtos/client-dto.model";

export class ClientView {
  dto: ClientDto = new ClientDto();
  editable: boolean = false;
}
