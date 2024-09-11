import {ActualTargetValue} from "./actual-target-value.model";

export class AssistancePlanEvaluation {
  total: ActualTargetValue[] = []

  tillToday: ActualTargetValue[] = []

  actualMonth: ActualTargetValue[] = []

  actualYear: ActualTargetValue[] = []

  notMatchingServices: number = 0

  notMatchingServicesIds: number[] = []
}
