import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-overview-permission-info-modal',
  templateUrl: './overview-permission-info-modal.component.html',
  styleUrls: ['./overview-permission-info-modal.component.css']
})
export class OverviewPermissionInfoModalComponent implements OnInit {
  readonly TITLE: string = "Berechtigungen"
  readonly CLOSE_BUTTON_DESCRIPTION: string = "Schließen"
  readonly TITLE_ADMIN: string = "Administrator"
  readonly DESCRIPTION_ADMIN: string = "Ein Administrator*in kann " +
    "alle Statistiken abfragen und als Einzige*r auch den Bereich 'alle'."
  readonly TITLE_EMPLOYEE: string = "Mitarbeiter"
  readonly DESCRIPTION_EMPLOYEE: string = "Ein*e Mitarbeiter*in benötigt das Zugriffsrecht 'Zugehörigkeit' " +
    "für den ausgewählten Bereich. Sollte dies nicht bestehen, wird die Anfrage verweigert. Der Bereich 'alle' " +
    "ist nur dem*r Administrator*in vorbehalten."

  constructor() { }

  ngOnInit(): void {
  }

}
