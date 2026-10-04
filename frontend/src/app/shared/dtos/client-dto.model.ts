import {InstitutionDto} from "./institution-dto.model";
import {CategoryTemplateDto} from "./category-template-dto.model";

export class ClientDto {
  id: number = 0
  firstName: string = ""
  lastName: string = ""
  phoneNumber: string = ""
  email: string = ""
  archived: boolean = false
  institution: InstitutionDto = new InstitutionDto()
  categoryTemplate: CategoryTemplateDto = new CategoryTemplateDto()
  institutionId: number = 0
  categoryTemplateId: number = 0
  categoryTemplateTitle: string = ""

  public toString = () : string => {
    return `${this.lastName} ${this.firstName}`;
  }
}
