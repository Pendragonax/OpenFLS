import {UnprofessionalDto} from "./unprofessional-dto.model";

export class SponsorDto {
  id: number = 0;
  name: string = "";
  payOverhang: boolean = false;
  payExact: boolean = false;
  unprofessionals: UnprofessionalDto[] = [];

  public constructor(init?:Partial<SponsorDto>) {
    Object.assign(this, init);
  }

}
