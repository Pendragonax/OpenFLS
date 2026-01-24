import {PermissionDto} from "./permission-dto.model";
import {ContingentDto} from "./contingent-dto.model";

export class InstitutionDto {
  id: number = 0;
  name: string = "";
  phonenumber: string = "";
  email: string = "";
  permissions: PermissionDto[] = [];

  public constructor(init?:Partial<InstitutionDto>) {
    Object.assign(this, init);
  }
}
