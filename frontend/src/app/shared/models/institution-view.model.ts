import {InstitutionDto} from "../dtos/institution-dto.model";

export class InstitutionViewModel {
  dto: InstitutionDto = new InstitutionDto();
  editable: boolean = false;
}
