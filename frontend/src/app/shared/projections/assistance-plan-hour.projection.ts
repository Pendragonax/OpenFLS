import {HourTypeSolo} from "./hour-type-solo.projection";
import {AssistancePlanSolo} from "./assistance-plan-solo.projection";
import {AssistancePlanHourDto} from "../dtos/assistance-plan-hour-dto.model";

export class AssistancePlanHour {
  id: number = 0;
  weeklyMinutes: number = 0;
  hourType: HourTypeSolo = new HourTypeSolo();
  assistancePlan: AssistancePlanSolo = new AssistancePlanSolo();

  static from(dto: AssistancePlanHourDto): AssistancePlanHour {
    const result = new AssistancePlanHour();
    result.weeklyMinutes = dto.weeklyMinutes;
    result.id = dto.id;

    result.assistancePlan = new AssistancePlanSolo();
    result.assistancePlan.id = dto.assistancePlanId;

    result.hourType = new HourTypeSolo();
    result.hourType.id = dto.hourTypeId;

    return result;
  }
}
