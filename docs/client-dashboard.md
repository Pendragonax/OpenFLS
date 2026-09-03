# Klient:innen-Dashboard

Das Dashboard fasst die für die tägliche Arbeit wichtigsten Informationen einer
Klient:in auf einer Seite zusammen. Es ersetzt die Bearbeitungsseite
(`/clients/detail/:id`) nicht, sondern steht ihr als Einstiegspunkt voran.

Route: `/clients/dashboard/:id`

## Inhalte und Zielansichten

| Kachel | Inhalt | Verlinkt auf |
| --- | --- | --- |
| Aktueller/Nächster/Aktuellster Hilfeplan | Ausgewählter Hilfeplan mit Zeitraum, Kostenträger, Einrichtung, Stundenmodus und Fortschrittsbalken (Titel benennt, welcher der drei Fälle vorliegt) | Hilfeplan-Bearbeitung (Stift-Icon), Zeitauswertungen, Ziele, Tabellarische Ansicht, Evaluationen |
| Letzte Einträge | Bis zu fünf für die anfragende Person sichtbare Einträge mit Datum, Titel, Inhalt und Mitarbeiter:in; ein Zähler zeigt, wie viele es tatsächlich sind | Eintragsliste, vorausgewählt auf den Zeitraum des angezeigten Hilfeplans |
| Aufgaben | Offene sowie paginiert jeweils zehn erledigte Aufgaben der Klient:in | Anlegen, Ändern, Abhaken, Löschen und Detailansicht mit Historie |

Der Kopfbereich erlaubt das Favorisieren der Klient:in und den Sprung in die
Stammdaten.

## Auswahl des „aktuellen“ Hilfeplans

Maßgeblich ist der Plan, der heute läuft (`start <= heute <= end`). Laufen mehrere
Pläne, gewinnt der mit dem spätesten Ende. Läuft keiner, wird der zuletzt beendete
Plan angezeigt, damit das Dashboard bei dokumentierter Vorgeschichte nicht leer
wirkt. Die Anzahl aller Pläne wird zusätzlich ausgewiesen.

## Hilfeplantypen im Fortschrittsbalken

Die Berechnung liegt gemeinsam mit der Favoritenansicht in
`frontend/src/app/shared/helpers/assistance-plan-progress.helper.ts`. Beide
Ansichten zeigen damit dieselben Zahlen.

- **EXACT**: Der Balken ist der Anteil der geleisteten an den bewilligten Stunden.
  Ab 95 % gilt der Plan als im Soll, zwischen 90 % und 95 % als Warnung.
- **CORRIDOR**: Der Korridor liegt fest zwischen 40 % und 60 % des Balkens.
  Innerhalb dieses Bereichs gilt der Plan als im Soll; darunter und darüber nicht.
  Angezeigt wird der bewilligte Bereich (von–bis) statt eines Einzelwerts.

Stundenwerte sind Time-Doubles: `1.30` bedeutet eine Stunde und 30 Minuten.

## Berechtigungen

Das Dashboard meldet je Abschnitt einen Zugriffszustand (`GRANTED` / `DENIED`)
zurück, damit fehlende Rechte im Frontend als solche erkennbar sind und nicht wie
fehlende Dokumentation wirken. Statt der Daten erscheint dann ein Hinweisfeld.

| Abschnitt | Sichtbar für |
| --- | --- |
| Stammdaten im Kopf | alle angemeldeten Mitarbeitenden |
| Hilfeplan-Kachel (inkl. Fortschrittsbalken) | Admin, Leitung, zugehörige oder lesende Mitarbeitende der Einrichtung der Klient:in |
| Letzte Einträge | wie Hilfeplan; zusätzlich wird je Eintrag gefiltert: eigene Einträge immer, fremde nur aus lesbaren Einrichtungen |
| Aufgaben | alle angemeldeten Mitarbeitenden |
| Aufgaben-Historie | alle angemeldeten Mitarbeitenden |

Archivierte Klient:innen und Pläne bleiben nur für Admins und Leitungskräfte
sichtbar. Das entspricht der bestehenden Regel der Klient:innen-Listen.

## Aufgaben

Aufgaben (`client_tasks`) hängen an einer Klient:in und tragen Titel,
Beschreibung, Fälligkeitsdatum, anlegende Person und Anlagezeitpunkt. Abgehakt
wird mit Kommentar und Erledigungsdatum.

Damit die Kachel nicht durch beliebig langen Freitext gesprengt wird, zeigt die
Kurzzeile im Dashboard bewusst wenig: bei offenen Aufgaben nur Titel, anlegende
Person mit Datum und Fälligkeitsdatum; bei abgehakten Aufgaben nur Titel und wer
sie wann erledigt hat. Beschreibung und Abhak-Kommentar erscheinen erst im
Detail-Modal, das sich per Klick auf die Aufgabe öffnet. Offene Aufgaben besitzen
getrennte Aktionen zum Ändern und Abhaken. Erledigte Aufgaben sind unveränderlich
und können nicht wieder geöffnet, aber weiterhin gelöscht werden. Die erledigten
Aufgaben werden serverseitig in Seiten zu je zehn Einträgen geladen.

Bewusste fachliche Entscheidung: **Aufgaben sind bewusst nicht
berechtigungsgefiltert.** Jede angemeldete Person darf sie sehen, anlegen und
abhaken, weil Aufgaben der einrichtungsübergreifenden Abstimmung dienen. Der
Ausgleich dafür ist die lückenlose Nachvollziehbarkeit: Jede Änderung schreibt
einen Eintrag in `client_task_audit_logs` mit Aktion, Zeitpunkt, handelnder Person
sowie Vorher-/Nachher-Werten. Aufgabentexte sollten deshalb keine besonders
schützenswerten Inhalte enthalten; die Oberfläche weist Aufgaben als
organisatorische Notizen aus, nicht als Falldokumentation.

Anlegen, Ändern, Abhaken und Löschen werden jeweils als eigene Audit-Aktion
protokolliert. In der Detailansicht erscheinen bewusst nur Änderungen und
Abhakvorgänge; Anlage- und Löschereignisse bleiben ausschließlich im Audit-Log.

Wird eine Klient:in archiviert, entfernt das System sie aus allen Favoritenlisten
(wie schon bisher bei den Hilfeplan-Favoriten). Wird eine Klient:in gelöscht,
räumt `ClientDeletionService` zuerst Favoriten und Aufgaben ab und löscht erst
danach die Klient:in; für jede entfernte Aufgabe entsteht ein Audit-Eintrag.

## Favoriten

Mitarbeitende favorisieren Klient:innen (`client_favorites`). Die Favoriten sind
der erste und damit voreingestellte Reiter der Startseite; ein Klick öffnet das
Dashboard. Die bestehenden Hilfeplan-Favoriten (`assistance_plan_favorites`)
bleiben unverändert und liegen weiterhin im Reiter „Hilfepläne“.

Jede Favoritenkarte zeigt, ob ein Hilfeplan aktiv ist, bis wann der letzte Plan
läuft und wie viele Aufgaben offen bzw. überfällig sind.

## Schnittstellen

| Methode | Pfad | Berechtigung |
| --- | --- | --- |
| GET | `/client_dashboards/client/{clientId}` | authentifiziert; Abschnitte werden einzeln freigegeben |
| GET | `/client_dashboards/favorites` | authentifiziert, liefert nur eigene Favoriten |
| POST | `/client_dashboards/favorites/client/{clientId}` | authentifiziert |
| DELETE | `/client_dashboards/favorites/client/{clientId}` | authentifiziert |
| GET | `/client_tasks/client/{clientId}` | authentifiziert |
| GET | `/client_tasks/client/{clientId}/completed?page=0&size=10` | authentifiziert |
| POST | `/client_tasks` | authentifiziert |
| PUT | `/client_tasks/{id}/change` | authentifiziert; nur offene Aufgaben |
| POST | `/client_tasks/{id}/complete` | authentifiziert |
| DELETE | `/client_tasks/{id}` | authentifiziert; offene und erledigte Aufgaben |
| GET | `/client_tasks/{id}/history` | authentifiziert; liefert Ändern und Abhaken |

## Migration

`V12__Client_Dashboard_Tasks_And_Favorites.sql` legt `client_favorites`,
`client_tasks` und `client_task_audit_logs` an. Bestehende Daten werden nicht
verändert; die Migration ist rein additiv.

## Bekannte Grenzen

- Das Dashboard lädt seine Abschnitte in einem Aufruf. Wächst der Umfang,
  sollten die Abschnitte auf eigene Endpunkte aufgeteilt werden.
- Aufgaben sind nicht Teil des Klient:innen-Archiv-Exports.
