import {EmployeeSolo} from "./employee-solo.projection";
import {InstitutionSoloDto} from "./institution-solo-dto.model";
import {ClientSolo} from "./client-solo.projection";

export class Service {
  id: number = 0;
  start: string = Date.now().toString();
  minutes: number = 0;
  title: string = "";
  content: string = "";
  groupOffer: boolean = false;
  institution: InstitutionSoloDto = new InstitutionSoloDto();
  employee: EmployeeSolo = new EmployeeSolo();
  client: ClientSolo = new ClientSolo();
}
