export class AssistancePlanPreviewDto {
  id: number = 0;
  clientId: number = 0;
  institutionId: number = 0;
  sponsorId: number = 0;
  clientFirstname: string = '';
  clientLastname: string = '';
  institutionName: string = '';
  sponsorName: string = '';
  start: string = '';
  end: string = '';
  isActive: boolean = false;
  isFavorite: boolean = false;
  approvedHoursPerWeek: number = 0;
  approvedHoursThisYear: number = 0;
  executedHoursThisYear: number = 0;
}
