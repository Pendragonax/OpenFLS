export class ClientAndDateResponseDto {
  clientId: number = 0;
  services: ClientAndDateServiceDto[] = [];
}

export class ClientAndDateServiceDto {
  timepoint: string = '';
  employeeName: string = '';
}
