import {EmployeeAccessDto} from "./employee-access-dto.model";
import {PermissionDto} from "./permission-dto.model";
import {ContingentDto} from "./contingent-dto.model";
import {UnprofessionalDto} from "./unprofessional-dto.model";

export class EmployeeDto {
  id: number = 0;
  firstName: string = "";
  lastName: string = "";
  phonenumber: string = "";
  email: string = "";
  description: string = "";
  inactive: boolean = false;
  institutionId: number | null = null;
  access: EmployeeAccessDto | null = new EmployeeAccessDto();
  permissions: PermissionDto[] = [];
  contingents: ContingentDto[] = [];
  unprofessionals: UnprofessionalDto[] = [];
}
