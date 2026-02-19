export class ClientAndDateResponseDto {
  clientId: number = 0;
  services: ClientAndDateServiceDto[] = [];
}

export class ClientAndDateServiceDto {
  id:number = 0;
  timepoint: string = '';
  employeeName: string = '';
}
