import {EmployeeDto} from "../dtos/employee-dto.model";
import {InstitutionDto} from "../dtos/institution-dto.model";

export class EmployeeViewModel {
  dto: EmployeeDto = new EmployeeDto();
  editable: boolean = false;
  administrator: boolean = false;
  leader: boolean = false;
  institutions: InstitutionDto[] = [];
}
