import {InstitutionDto} from "./institution-dto.model";
import {CategoryTemplateDto} from "./category-template-dto.model";
import {AssistancePlanDto} from "./assistance-plan-dto.model";

export class ClientDto {
  id: number = 0
  firstName: string = ""
  lastName: string = ""
  phoneNumber: string = ""
  email: string = ""
  institution: InstitutionDto = new InstitutionDto()
  categoryTemplate: CategoryTemplateDto = new CategoryTemplateDto()
  assistancePlans: AssistancePlanDto[] = []

  public toString = () : string => {
    return `${this.lastName} ${this.firstName}`;
  }
}
