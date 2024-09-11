export class HourTypeDto {
  id: number = 0;
  title: string = "";
  price: number = 0;

  public constructor(init?:Partial<HourTypeDto>) {
    Object.assign(this, init);
  }
}
