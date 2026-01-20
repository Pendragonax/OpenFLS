import {PermissionDto} from "./permission-dto.model";

export class UpdateInstitutionDto {
  id: number = 0;
  name: string = "";
  phonenumber: string = "";
  email: string = "";
  permissions: PermissionDto[] = [];
}
