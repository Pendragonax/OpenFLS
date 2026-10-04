
import {ClientDto} from "../dtos/client-dto.model";

export class ClientViewModel {
  dto: ClientDto = new ClientDto();
  editable: boolean = false;
  favorite: boolean = false;
}
