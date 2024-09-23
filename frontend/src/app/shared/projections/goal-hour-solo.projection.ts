import {HourTypeSolo} from "./hour-type-solo.projection";

export class GoalHour {
  id: number = 0;
  weeklyHours: number = 0.0;
  hourType: HourTypeSolo = new HourTypeSolo();
}
