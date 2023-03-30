import {HourTypeDto} from "./hour-type-dto.model";

export class ActualTargetValue {
  target: number = 0.0

  actual: number = 0.0

  size: number = 0

  hourType: HourTypeDto | null = null
}
