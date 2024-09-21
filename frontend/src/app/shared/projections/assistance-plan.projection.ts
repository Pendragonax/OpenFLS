import {ClientSolo} from "./client-solo.projection";
import {SponsorSolo} from "./sponsor-solo.projection";
import {InstitutionSolo} from "./institution-solo.projection";
import {AssistancePlanHour} from "./assistance-plan-hour.projection";
import {Goal} from "./goal.projection";

export class AssistancePlan {
  id: number = 0;
  start: string = Date.now().toLocaleString();
  end: string = Date.now().toLocaleString();
  client: ClientSolo = new ClientSolo();
  sponsor: SponsorSolo = new SponsorSolo();
  institution: InstitutionSolo = new InstitutionSolo();
  hours: AssistancePlanHour[] = [];
  goals: Goal[] = [];
}
