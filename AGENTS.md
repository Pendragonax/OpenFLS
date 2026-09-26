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
- Die Migration auf die Regeln zu Service-Arten, Entity-Weitergabe, Projections
  und DTO-Benennung erfolgt Domäne für Domäne, Abhängigkeits-Blätter zuerst.
  Pro Domäne: 1. Inventur (Entities, DTOs, Projections, Service-Methoden,
  Endpunkte), 2. Vorschlag mit Umbenennungen, 3. Freigabe, 4. Umsetzung
  einschließlich Angular-Anpassung und `./gradlew clean build`.

### Schichten und Abhängigkeiten

- Controller sind schlank: Sie nehmen HTTP-Anfragen entgegen, validieren
  Eingaben am Rand, rufen Services auf und übersetzen Ergebnisse in HTTP-
  Antworten. Sie enthalten keine Fachlogik und keinen direkten Datenbankzugriff.
- Services bilden fachliche Anwendungsfälle ab. Sie orchestrieren Regeln,
  Transaktionen, Berechtigungsprüfungen und die Zusammenarbeit von Repositories.
- Repositories kapseln jeden Persistenzzugriff. Spring-Data-
  `CrudRepository`/`JpaRepository` sind als interne JPA-Repräsentation erlaubt.
- JPA-Entities dürfen innerhalb eines Services zum Lesen, Ändern und Speichern
  verwendet werden. Sie gelangen nie an einen RestController und nie in eine
  HTTP-Antwort. Zwischen Services dürfen sie nur über gekennzeichnete Methoden
  (siehe „Entity-Weitergabe zwischen Services“) weitergegeben werden.
- Die erlaubte Richtung lautet grundsätzlich:
  `Controller → Service → Repository`. Keine Schicht umgeht die darunterliegende.
- Domänen kommunizieren über klar definierte Services. Für die Verarbeitung
  dürfen Services untereinander Entities austauschen, aber ausschließlich über
  Methoden, die mit `@InternalEntityApi` gekennzeichnet sind.

### Service-Arten

Services werden nach Zuständigkeit getrennt und bilden jeweils eigene Klassen.

- **CRUD-Service** (`<Domain>Service`): Anlegen, Ändern, Löschen und Lesen der
  Aggregate einer Domäne. Er ist der einzige Service, der die Repositories der
  Domäne für diese Aggregate nutzt, und stellt bei Bedarf gekennzeichnete
  Entity-Methoden für andere Services bereit.
- **Anwendungsfall-Service** (nach Zweck benannt, z. B. `<Domain>EvaluationService`,
  `<Domain>PreviewService`, `<Domain>ArchiveService`): bildet einen weiterführenden
  Anwendungsfall ab, der Fachlogik über mehrere Aggregate oder Domänen hinweg
  ausführt und dafür Entities benötigt. Er bezieht Entities über die
  gekennzeichneten Methoden der CRUD-Services und liefert nach außen DTOs.
- CRUD- und Anwendungsfall-Logik werden nicht in derselben Klasse vermischt.

### Entity-Weitergabe zwischen Services

- Jede öffentliche Service-Methode, die eine JPA-Entity (oder eine Collection
  davon) herausgibt oder entgegennimmt, wird mit der Annotation
  `@InternalEntityApi` versehen. Methoden ohne diese Annotation geben niemals
  Entities heraus.
- `@InternalEntityApi`-Methoden dürfen nur von anderen Services (Klassen mit
  `@Service`) aufgerufen werden, niemals von einem `@RestController`.
- Ein ArchUnit-Test (ArchTest) stellt dies automatisiert sicher und prüft
  zusätzlich, dass RestController weder Entities noch Projections verwenden.
  Der Test wird zusammen mit der Annotation eingeführt und für jede migrierte
  Domäne eingehalten.
- Die Benennung entitätsbasierter Methoden bleibt einheitlich:
  `getEntityById`, `getAllEntities`, `createEntity`, `updateEntity`.

### Projections

- Projections (Spring-Data-Interface-Projections) dienen ausschließlich dazu,
  Daten gezielt und ohne Lazy-Loading-Nachladen zu laden und so Ladezeiten zu
  reduzieren.
- Sie bleiben intern: Sie werden nur zwischen Repository und Service verwendet.
  Sie werden nie zwischen Services ausgetauscht und nie an RestController
  gegeben. Nach außen gibt der Service DTOs zurück.
- Eine Projection gehört der Domäne, deren Repository sie abfragt. Wird sie
  in der Domäne der referenzierten Entity nicht selbst verwendet, liegt sie in
  der Domäne, die sie braucht (auch verschachtelte Projections).
- Als `*Projection` heißt nur, was tatsächlich von einem Repository als
  Spring-Data-Projection abgefragt wird. Von Hand aus Entities gebaute
  Lesemodelle sind DTOs und liegen in `dtos/`.

### DTO-Regeln

JPA-Entities sind ein internes Persistenzdetail und dürfen weder über eine
öffentliche Service-API zu Controllern noch über Controller nach außen gelangen.

- Die von Spring Data geerbten CRUD-Methoden dürfen innerhalb des zugehörigen
  Services verwendet werden. Ihre Entity-Rückgaben werden dort in explizite,
  zweckgebundene DTOs übersetzt.
- Benutzerdefinierte Leseabfragen liefern bevorzugt DTOs oder Projections,
  wenn kein Entity-Zugriff für eine Änderung benötigt wird.
- Repository-DTOs werden nach dem benötigten Anwendungsfall benannt und nicht
  als generische Entity-Kopien angelegt.

**Namenskonvention (verbindlich):**

- Grundform: `<X>Dto` ist die flache Form (Skalare und Fremdschlüssel-Ids,
  keine verschachtelten Objekte anderer Domänen). Erweiterungen mit einer oder
  zwei Relationen heißen `<X>With<Y>`. Ab drei Relationen oder bei eigenem
  Zweck wird nach dem Anwendungsfall benannt. Die Begriffe `Solo`, `Simple`
  und `XL` werden nicht verwendet.
- DTOs an der HTTP-Grenze tragen `Request` (eingehend) oder `Response`
  (ausgehend) im Namen, ergänzt um den Anwendungsfall, z. B.
  `AbsenceCreateRequest`, `AbsenceUpdateRequest`, `AbsenceResponse`,
  `AbsenceWithClientResponse`. Der Anwendungsfall steht vor dem Suffix.
- DTOs, die nur innerhalb eines Services verwendet werden, tragen weder
  `Request` noch `Response` im Namen (`<X>Dto`, `<X>With<Y>`). So ist an jedem
  Namen erkennbar, ob ein Typ die HTTP-Grenze überschreitet.
- Ein- und ausgehende Typen werden nicht wiederverwendet: Ein Lese-DTO dient
  nie zugleich als Eingabe.

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
- Erwartete Fälle (z. B. Ressource nicht gefunden, Pfad-Id passt nicht zum
  Request) werden nicht per Exception, sondern direkt als passende Response
  zurückgegeben (z. B. `404`, `400`). Exceptions sind unerwarteten Fehlern
  vorbehalten.
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
