import {ContingentEmployeeEvaluationDto} from "./contingent-employee-evaluation-dto.model";

export class ContingentEvaluationDto {
  institutionId: number = 0
  employees: ContingentEmployeeEvaluationDto[] = []

}
