import {Component, Input, OnInit} from '@angular/core';
import {ReplaySubject} from "rxjs";

@Component({
  selector: 'app-overview-value-type-info-modal',
  templateUrl: './overview-value-type-info-modal.component.html',
  styleUrls: ['./overview-value-type-info-modal.component.css']
})
export class OverviewValueTypeInfoModalComponent implements OnInit {
  readonly TITLE: string = "Wertermittlung"
  readonly CLOSE_BUTTON_DESCRIPTION: string = "Schließen"
  readonly EXECUTED_TITLE: string = "geleistete Stunden"
  readonly EXECUTED_DESCRIPTION: string = "Geleistete Stunden umfassen die Summe aller Minuten der Einträge, " +
    "die den jeweiligen Filtern entsprechen."
  readonly APPROVED_TITLE: string = "genehmigte Stunden"
  readonly APPROVED_DESCRIPTION: string = "Genehmigte Stunden werden folgendermaßen berechnet. " +
    "Sofern ein Hilfeplan über eingetragene Stunden verfügt, so werden diese für die Berechnung herangezogen. " +
    "Sollten dort keine Eintragen sein, dann werden die Ziele und deren Stunden für die Berechnung genutzt. " +
    "Bitte achten sie dabei auf die korrekt angelegten Hilfepläne. " +
    "Für die Ermittlung werden die ausgewählten Filter berücksichtigt."

  constructor() { }

  ngOnInit(): void { }
}
