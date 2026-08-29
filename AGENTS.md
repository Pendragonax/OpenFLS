# AGENTS.md – OpenFLS

## Projektzweck

OpenFLS ist eine Dokumentations- und Datenerfassungssoftware für soziale
Einrichtungen. Mitarbeitende dokumentieren geleistete Stunden mit Klient:innen
auf Grundlage von Hilfeplänen. Die Dokumentation umfasst insbesondere Zeiten,
Ziele, Inhalte, Fortschritte und weitere abrechnungsrelevante Angaben.

Die erfassten Daten sind die Grundlage für:

- nachvollziehbare Falldokumentation und Hilfeplanarbeit,
- Kostenabrechnung und Nachweise gegenüber Kostenträgern,
- Auswertungen zur Auslastung und zum Ressourceneinsatz,
- fachliche und betriebliche Statistiken.

Die fachliche Nachvollziehbarkeit, Datenqualität, Datenschutz und eine klare
Trennung der Verantwortlichkeiten haben Vorrang vor kurzfristigen technischen
Abkürzungen.

## Projektstruktur

Das Projekt besteht aus einem Frontend und einem Backend. Änderungen werden in
dem Teil umgesetzt, dem sie fachlich und technisch zugeordnet sind. Gemeinsame
Verträge werden bewusst und kompatibel weiterentwickelt.

Das aktuelle Frontend ist Angular. React ist eine mögliche spätere Migration;
bis zu einer ausdrücklich beauftragten Migration bleibt Angular die verbindliche
Frontend-Technologie.

```text
/
├── frontend/              # Benutzeroberfläche
├── backend/               # Fachlogik und Persistenzzugriff
├── docker/                # Container- und Compose-Konfigurationen je Scope
└── docs/                  # Architektur-, Betriebs- und Fach-Dokumentation
```

## Fachliche Modellierung

- Fachbegriffe aus dem sozialen Kontext werden einheitlich verwendet, z. B.
  Klient:in, Hilfeplan, Ziel, Leistung, Zeiterfassung, Dokumentation und
  Abrechnung.
- Jede fachlich relevante Änderung muss prüfen, welche Auswirkungen sie auf
  Auswertungen, Kostenabrechnung, Datenschutz und historische Nachvollziehbarkeit
  hat.
- Zeitangaben brauchen klare Semantik (Zeitzone, Beginn, Ende, Dauer,
  Korrekturstatus). Dauer wird nicht aus widersprüchlichen Daten abgeleitet.
- Fachliche Regeln und Validierungen gehören ins Backend. Das Frontend ergänzt
  sie mit verständlicher Nutzerführung, ersetzt sie aber nicht.
- Personenbezogene und besonders schützenswerte Daten werden nur verarbeitet,
  angezeigt und protokolliert, soweit dies für die jeweilige Aufgabe nötig ist.
  Bei Änderungen sind Rollen, Berechtigungen und Datenminimierung zu prüfen.
- Dokumentation, Hilfepläne und abrechnungsrelevante Daten werden grundsätzlich
  erhalten. Korrekturen und ausnahmsweise Löschungen müssen fachlich begründet
  und nachvollziehbar sein.
- Archivierte Inhalte bleiben für berechtigte Personen verfügbar und müssen in
  den vorgesehenen Exporten enthalten sein, soweit sie vom Exportumfang erfasst
  sind.
- Neue fachliche Funktionen erhalten ein Audit-Log für relevante Änderungen.
  Bestehender Code wird nur dann mit Audit-Logging nachgerüstet, wenn dies
  ausdrücklich beauftragt ist oder im Rahmen der Änderung erforderlich wird.

## Backend-Architektur

Das Backend ist domain-orientiert strukturiert. Fachliche Domänen sind die
oberste Organisationsgrenze; technische Schichten werden innerhalb einer Domäne
angeordnet.

```text
backend/src/main/.../
└── domains/
    └── <domain>/
        ├── controller/    # HTTP-Schnittstelle
        ├── service/       # Anwendungsfälle und Fachlogik
        ├── repository/    # Datenzugriff
        ├── dto/           # Verträge zwischen Schichten und nach außen
        └── entity/        # JPA-Persistenzmodell, falls erforderlich
```

Die konkrete Paketstruktur kann sich an bestehende Konventionen anpassen, muss
aber diese Verantwortungsgrenzen erhalten.

### Übergangsstrategie

- Es gibt keine einmalige Komplettmigration der bestehenden Codebasis.
- Neuer Code sowie Code, der im jeweiligen Auftrag fachlich oder technisch
  angefasst werden muss, erfüllt diese Architekturregeln.
- Unveränderter Bestandscode darf bestehen bleiben. Neue Umgehungen der
  Architekturgrenzen werden nicht eingeführt.
- Größere Umstrukturierungen oder die Migration bestehender Domänen erfolgen nur
  bei einem ausdrücklichen Auftrag.

### Schichten und Abhängigkeiten

- Controller sind schlank: Sie nehmen HTTP-Anfragen entgegen, validieren
  Eingaben am Rand, rufen Services auf und übersetzen Ergebnisse in HTTP-
  Antworten. Sie enthalten keine Fachlogik und keinen direkten Datenbankzugriff.
- Services bilden fachliche Anwendungsfälle ab. Sie orchestrieren Regeln,
  Transaktionen, Berechtigungsprüfungen und die Zusammenarbeit von Repositories.
- Repositories kapseln jeden Persistenzzugriff. Spring-Data-
  `CrudRepository`/`JpaRepository` sind als interne JPA-Repräsentation erlaubt.
- JPA-Entities dürfen innerhalb eines Services zum Lesen, Ändern und Speichern
  verwendet werden, verlassen aber niemals dessen öffentliche Service-Grenze.
- Die erlaubte Richtung lautet grundsätzlich:
  `Controller → Service → Repository`. Keine Schicht umgeht die darunterliegende.
- Domänen kommunizieren über klar definierte Services oder explizite DTOs; es
  dürfen keine JPA-Entities domänenübergreifend weitergereicht werden.

### DTO-Regel für Persistenzzugriff

JPA-Entities sind ein internes Persistenzdetail und dürfen weder über eine
öffentliche Service-API noch über Controller nach außen gelangen.

- Die von Spring Data geerbten CRUD-Methoden dürfen innerhalb des zugehörigen
  Services verwendet werden. Ihre Entity-Rückgaben werden dort in explizite,
  zweckgebundene DTOs übersetzt.
- Benutzerdefinierte Leseabfragen liefern bevorzugt DTOs oder Projektionen,
  wenn kein Entity-Zugriff für eine Änderung benötigt wird.
- Services und Controller geben niemals JPA-Entities weiter.
- Repository-DTOs werden nach dem benötigten Anwendungsfall benannt und nicht
  als generische Entity-Kopien angelegt.

## Frontend

- Das Frontend stellt die fachlichen Arbeitsabläufe verständlich, zugänglich und
  fehlertolerant dar; besonders wichtig sind Zeiterfassung, Hilfeplandokumentation
  und Auswertungen.
- API-Verträge werden typisiert und explizit modelliert. Fachliche Berechnungen
  mit Abrechnungswirkung bleiben im Backend maßgeblich.
- Formulare zeigen Validierungsfehler klar am Eingabepunkt und vermeiden
  Datenverlust bei Fehlern oder unvollständigen Eingaben.
- Anzeige und Bearbeitung personenbezogener Daten richten sich nach den
  Berechtigungen der angemeldeten Person.

## REST-API und Berechtigungen

- Mit Ausnahme der Anmeldung ist jeder REST-Endpunkt authentifiziert und mit
  einer fachlich eindeutig definierten rollenbasierten Berechtigung abgesichert.
  Ein Endpunkt wird nicht freigegeben, bevor diese Berechtigung geklärt ist.
- Fehlerantworten verwenden einen festgelegten HTTP-Status und einen stabilen,
  maschinenlesbaren Fehlercode. Das Frontend reagiert auf diesen Code gezielt;
  Fehlermeldungen ersetzen keine fachliche Fehlerbehandlung.
- Änderungen an API-Verträgen werden im selben Auftrag im Angular-Frontend
  angepasst. Erst bei einer beauftragten React-Migration gilt dies entsprechend
  für React.
- Paginierte Abfragen werden als ausdrücklich angefragte, separate Endpunkte
  angeboten. Nicht paginierte Endpunkte bleiben klar abgegrenzt und dürfen nicht
  stillschweigend ihr Antwortformat ändern.
- OpenAPI ist als künftige Vertragsdokumentation vorgesehen. Seine Einführung
  oder Erweiterung erfolgt gezielt und nicht als Nebenwirkung einer Änderung.

## Docker und Betriebsumgebungen

Für jede Änderung, die Laufzeit, Konfiguration, Netzwerke, Datenhaltung oder
Build-Artefakte betrifft, sind mehrere Docker-Szenarien zu betrachten. Eine
einzige Compose-Datei ist nicht automatisch für alle Zwecke geeignet.

Mindestens diese Scopes werden unterschieden:

| Scope | Ziel |
| --- | --- |
| Lokal | Schnelle Entwicklung mit sinnvollen Defaults und nachvollziehbarer Konfiguration. |
| Entwicklung/Integration | Reproduzierbares Zusammenspiel aller benötigten Dienste und automatisierbare Tests. |
| Staging | Produktionsnahes Verhalten zur Abnahme, ohne produktive Daten oder Geheimnisse. |
| Produktion | Sichere, schlanke, versionierte und beobachtbare Auslieferung mit externer Konfiguration. |

Die vorhandenen Compose-Dateien haben folgende Bedeutung:

| Datei | Verwendung |
| --- | --- |
| `docker-compose.yml` | Produktion beim Kunden. |
| `docker-compose-local.yml` | Produktionsnaher lokaler Betrieb mit lokalen Images. |
| `docker-compose-dev.yml` | Entwicklung von Frontend und Backend mit Hot Reload. |
| `docker-compose*.ssl.yml` | Jeweiliger Scope mit HTTPS-/SSL-Unterstützung. |

- Bestehende Dockerfiles und Compose-Dateien je Scope werden vor Änderungen
  geprüft und konsistent gehalten.
- Images sind reproduzierbar, möglichst schlank und laufen nicht als root, sofern
  es keinen dokumentierten Grund dagegen gibt.
- Geheimnisse, Zugangsdaten und produktive Konfigurationen gehören nicht in
  Images oder versionierte Compose-Dateien. Sie werden über geeignete sichere
  Konfiguration bereitgestellt.
- Persistente Daten, Migrationen, Health Checks, Logs, Backups und nötige
  Abhängigkeiten sind je Scope bewusst zu behandeln.
- Für die Produktionsumgebung beim Kunden wird jeder Eingriff in Datenhaltung
  oder Backup gegen einen dokumentierten Wiederherstellungsvorgang geprüft. Ein
  Backup gilt erst als belastbar, wenn seine Wiederherstellung regelmäßig
  erfolgreich erprobt wurde.
- Das Produktionsziel ist, das Frontend künftig zusammen mit dem Backend
  auszuliefern, statt einen separaten Frontend-Container zu betreiben. Bis diese
  Migration ausdrücklich beauftragt ist, bleiben die bestehenden Container-
  Grenzen erhalten.
- Produktionsspezifische Optimierungen dürfen die lokale Entwicklung nicht
  unnötig erschweren; Unterschiede werden dokumentiert statt versteckt.

## Tests und Qualität

- Tests werden nach dem Risiko und dem Nutzen der Änderung eingerichtet – nicht,
  um blind einen pauschalen Prozentwert zu erreichen.
- Fachliche Regeln, Berechnungen, Abrechnungslogik, Berechtigungen,
  Zeitberechnungen und Datenmappings erhalten gezielte automatisierte Tests.
- Für geänderte Schnittstellen werden passende Controller-/API-Tests ergänzt;
  für Persistenzabfragen Repository- oder Integrationstests, wenn deren Aussage
  relevant ist.
- Für Frontend-Änderungen werden Komponenten, Services und kritische
  Nutzerabläufe entsprechend ihrem Risiko getestet.
- Die Testauswahl und nicht abgedeckte Risiken werden in der Übergabe genannt.
  Eine Testabdeckung als Kennzahl ist nur dann einzurichten oder zu verschärfen,
  wenn sie für den konkreten Bereich eine sinnvolle Qualitätsaussage liefert.
- Vor Abschluss läuft mindestens die kleinste aussagekräftige Testsuite für die
  Änderung. Nicht ausgeführte Tests und Gründe dafür werden transparent genannt.

## Arbeitsweise bei Änderungen

1. Auftrag, fachlichen Kontext, Akzeptanzkriterien, betroffene Domänen und
   erforderliche Rollen/Berechtigungen bestimmen.
2. Bei Auswirkungen auf Personen-, Abrechnungs- oder Statistikdaten Risiken und
   Migrationsbedarf klären, bevor implementiert wird.
3. Kleine, überprüfbare Schritte umsetzen und nach jedem Schritt auf
   Architekturgrenzen, Fehlerfälle und Regressionen prüfen.
4. Die passenden Tests ausführen und Ergebnis sowie Restrisiken dokumentieren.
5. Bei Backend- oder Laufzeitänderungen die betroffenen Docker-Scopes prüfen.

### Verbindliche Backend-Prüfung

Nach jeder Änderung am Backend ist zusätzlich zu gezielten Tests immer ein
vollständiger `./gradlew clean build` im Verzeichnis `backend/` auszuführen.
Die Arbeit gilt erst als geprüft, wenn dieser Clean-Build ohne Fehler
durchläuft; verbleibende Warnungen werden in der Übergabe dokumentiert.

## Dokumentation und Entscheidungen

- Architekturentscheidungen, fachliche Annahmen, Migrationshinweise und bewusst
  akzeptierte Risiken werden in der Projektdokumentation festgehalten.
- Bestehende Entscheidungen werden nicht überschrieben, sondern ergänzt.
- Für Spring Boot, Spring Data, Spring Security oder Angular werden bevorzugt
  die jeweiligen offiziellen Dokumentationsquellen verwendet.
- Für alle Änderungen am Logging ist der verbindliche [Logging Guide](docs/logging-guide.md)
  zu beachten. Er definiert Mindestfelder, Log-Level, Datenschutz, Audit-/Security-
  Trennung sowie Aufbewahrung und Löschung.
