import {InstitutionDto} from "./institution-dto.model";
import {CategoryTemplateDto} from "./category-template-dto.model";
import {AssistancePlanForServiceEditingDto} from "./assistance-plan-for-service-editing-dto.model";

export class ClientForServiceEditingDto {
  id: number = 0
  firstName: string = ""
  lastName: string = ""
  phoneNumber: string = ""
  email: string = ""
  archived: boolean = false
  institution: InstitutionDto = new InstitutionDto()
  categoryTemplate: CategoryTemplateDto = new CategoryTemplateDto()
  assistancePlans: AssistancePlanForServiceEditingDto[] = []

  public toString = () : string => {
    return `${this.lastName} ${this.firstName}`;
  }
}

