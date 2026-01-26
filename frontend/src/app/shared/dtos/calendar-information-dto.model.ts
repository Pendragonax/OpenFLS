import {AssistancePlanHourResponseDto} from "./assistance-plan-hour-response-dto.model";
import {GoalResponseDto} from "./goal-response-dto.model";


class CalendarDayInformationDTO {
  date: string = "";
  serviceCount: number = 0;
  executedHours: number = 0;
  executedMinutes: number = 0;
  contingentHours: number = 0;
  contingentMinutes: number = 0;
  differenceHours: number = 0;
  differenceMinutes: number = 0;
}

export class ContingentInformationDTO {
  executedPercentage: number = 0;
  warningPercent: number = 0;
  executedHours: number = 0;
  executedMinutes: number = 0;
  contingentHours: number = 0;
  contingentMinutes: number = 0;
  differenceHours: number = 0;
  differenceMinutes: number = 0;
}

export class CalendarInformationDTO {
  employeeId: number = 0;
  days: CalendarDayInformationDTO[] = [];
  today: ContingentInformationDTO = new ContingentInformationDTO();
  lastWeek: ContingentInformationDTO = new ContingentInformationDTO()
  lastMonth: ContingentInformationDTO = new ContingentInformationDTO();
}
