export class EvaluationDto {
  id: number = 0;
  goalId: number = 0;
  date: string = "";
  content: string = "";
  approved: boolean = false;
  createdBy: string = "";
  createdAt: Date = new Date(Date.now());
  updatedBy: string = "";
  updatedAt: Date = new Date(Date.now());
}
