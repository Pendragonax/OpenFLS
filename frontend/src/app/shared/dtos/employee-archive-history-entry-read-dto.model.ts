export class EmployeeArchiveHistoryEntryReadDto {
  id: number = 0;
  actionType: string = '';
  actionDate: string = '';
  actionTimestamp: string = '';
  reason: string = '';
  remark: string = '';
  executingEmployeeId: number = 0;
  executingEmployeeFirstname: string = '';
  executingEmployeeLastname: string = '';
}
