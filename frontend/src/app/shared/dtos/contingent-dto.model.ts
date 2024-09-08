export class ContingentDto {
  id: number = 0;
  start: string = Date.now().toLocaleString();
  end: string | null = null;
  weeklyServiceHours: number = 0;
  employeeId: number = 0;
  institutionId: number = 0;
}
