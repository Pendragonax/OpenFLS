import {Service} from "../../../dtos/service.projection";

export class ServiceExport {
  id: number = 0;
  start: string = Date.now().toString();
  minutes: number = 0;
  title: string = "";
  content: string = "";
  institutionName: string = "";
  employeeName: string = ""
  clientName: string = "";

  static of(service: Service): ServiceExport {
    return {
      id: service.id,
      start: service.start,
      minutes: service.minutes,
      title: service.title,
      content: service.content,
      institutionName: service.institution.name,
      employeeName: service.employee.lastname + ", " + service.employee.firstname,
      clientName: service.client.lastName + ", " + service.client.firstName
    }
  }

  static arrayStringOf(service: Service): string[] {
    return [
      service.id.toString(),
      service.start,
      service.minutes.toString(),
      service.title,
      service.content,
      service.institution.name,
      service.employee.lastname + ", " + service.employee.firstname,
      service.client.lastName + ", " + service.client.firstName
    ]
  }

  static header(): string[] {
    return ["Id", "Start", "Minuten", "Titel", "Inhalt", "Institution", "Mitarbeiter", "Klient"];
  }
}
