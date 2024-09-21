import {GoalHour} from "./goal-hour-solo.projection";

export class Goal {
  id: number = 0;
  title: string = "";
  description: string = "";
  assistancePlanId: number = 0;
  hours: GoalHour[] = [];
}
