import {ClientSolo} from "./client-solo.projection";
import {SponsorSolo} from "./sponsor-solo.projection";
import {InstitutionSolo} from "./institution-solo.projection";
import {AssistancePlanHour} from "./assistance-plan-hour.projection";
import {Goal} from "./goal.projection";
import {AssistancePlanHourMode} from "../dtos/assistance-plan-hour-mode.model";
import {HourCorridorModel} from "../dtos/hour-corridor-model";

export class AssistancePlan {
  id: number = 0;
  start: string = Date.now().toLocaleString();
  end: string = Date.now().toLocaleString();
  client: ClientSolo = new ClientSolo();
  sponsor: SponsorSolo = new SponsorSolo();
  institution: InstitutionSolo = new InstitutionSolo();
  hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT;
  hourCorridor: HourCorridorModel | null = null;
  hours: AssistancePlanHour[] = [];
  goals: Goal[] = [];
}
