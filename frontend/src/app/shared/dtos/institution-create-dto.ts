import {PermissionDto} from "./permission-dto.model";

export class CreateInstitutionDto {
  name: string = "";
  phonenumber: string = "";
  email: string = "";
  permissions: PermissionDto[] = [];
}
