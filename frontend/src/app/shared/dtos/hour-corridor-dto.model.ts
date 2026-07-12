export class HourCorridorDto {
  id: number = 0;
  title: string = '';
  weeklyMinutesFrom: number = 0;
  weeklyMinutesTill: number = 0;
  hourTypeId: number = 0;
  hourTypeTitle: string = '';
  assistancePlanCount: number = 0;

  public constructor(init?: Partial<HourCorridorDto>) {
    Object.assign(this, init);
  }
}
