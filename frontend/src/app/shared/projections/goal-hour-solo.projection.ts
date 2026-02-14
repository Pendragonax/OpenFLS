import {HourTypeSolo} from "./hour-type-solo.projection";

export class GoalHour {
  id: number = 0;
  weeklyMinutes: number = 0;
  hourType: HourTypeSolo = new HourTypeSolo();
}
