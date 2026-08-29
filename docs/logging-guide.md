# Logging Guide für OpenFLS

Dieser Guide definiert verbindliche Regeln für neues und geändertes Logging in OpenFLS. Er gilt für Backend, Frontend, Integrationen, Betriebslogs sowie Audit- und Security-Logs.

## Grundsätze

- Logs dienen der Fehleranalyse, Betriebsüberwachung, Sicherheitsanalyse und nachvollziehbaren Fachprozessen.
- Vor jedem neuen Logeintrag ist der Zweck zu klären: Diagnose, Betrieb, Security oder Audit.
- Es wird so viel wie nötig und so wenig wie möglich protokolliert. Personenbezogene und besonders schützenswerte Daten werden minimiert.
- Fachliche Audit-Ereignisse werden nicht durch technische Diagnose-Logs ersetzt.
- Logs sind strukturierte, maschinenlesbare Ereignisse und keine Sammlung beliebiger Debug-Ausgaben.
- Zeitstempel werden in UTC und ISO-8601 mit Millisekunden gespeichert.

## Was protokolliert werden muss

Mindestens zu erfassen sind:

- erfolgreiche und fehlgeschlagene Anmeldungen,
- fehlgeschlagene Autorisierungs- und Berechtigungsprüfungen,
- Session-, Token- und CSRF-Fehler,
- Eingabe- und Ausgabevalidierungsfehler,
- Anwendungs-, Datenbank-, Datei-, Netzwerk- und Fremdsystemfehler,
- Start, Shutdown und Initialisierung wichtiger Komponenten,
- Änderungen an sicherheits- oder betriebsrelevanter Konfiguration,
- Admin-Aktionen und Änderungen an Rollen und Berechtigungen,
- Importe, Exporte und Zugriffe auf besonders schützenswerte Daten,
- fachlich relevante Änderungen an Dokumentationen, Hilfeplänen, Leistungen und Abrechnungsdaten,
- verdächtige oder fachlich unplausible Abläufe.

Nicht jede erfolgreiche Standardabfrage benötigt einen eigenen Logeintrag. Für fachliche Nachvollziehbarkeit ist jedoch ein separates Audit-Ereignis zu verwenden.

## Pflichtfelder eines Ereignisses

Jeder relevante Eintrag enthält, soweit verfügbar:

| Feld | Bedeutung |
| --- | --- |
| `timestamp` | UTC-Zeitpunkt des Ereignisses bzw. der Protokollierung |
| `level` | `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `event_name` | stabiler, punktierter Ereignisname, z. B. `user.login.failed` |
| `outcome` | `success`, `failure`, `denied`, `cancelled` oder `unknown` |
| `message` | kurze menschenlesbare Zusammenfassung |
| `service` / `application` | erzeugender Dienst und Anwendung |
| `logger` | technische Quelle bzw. Klasse |
| `correlation_id` | Zuordnung zu einem Request oder Vorgang |
| `trace_id` / `span_id` | Verknüpfung mit Distributed Tracing, sofern vorhanden |
| `user_id` | interne oder pseudonymisierte Benutzer-ID, niemals unnötige Klardaten |
| `object_id` | betroffene Ressource, sofern für die Rekonstruktion erforderlich |
| `exception.type` | Exception-Typ bei Fehlern |
| `exception.stacktrace` | Stacktrace bei Fehlern, ohne Geheimnisse oder sensible Nutzdaten |

Felder werden flach und konsistent benannt. Neue Felder brauchen einen klaren Auswertungszweck. Die Benennung soll sich an den [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/) orientieren.

## Log-Level

- `ERROR`: Der Vorgang ist fehlgeschlagen oder ein nicht automatisch behebbarer Systemfehler liegt vor.
- `WARN`: Unerwartetes, behandeltes oder risikobehaftetes Verhalten.
- `INFO`: Wichtige Lebenszyklus-, Security-, Betriebs- und fachliche Ereignisse.
- `DEBUG`: Technische Diagnoseinformationen für Entwicklung und gezielte Fehlersuche.
- `TRACE`: Sehr detaillierte, kurzfristige Analyse; nicht dauerhaft in Produktion aktivieren.

Ein Fehler darf nicht auf mehreren Schichten ohne zusätzlichen Kontext mehrfach geloggt werden. Exception-Stacktraces gehören normalerweise an die Stelle, an der der Fehler behandelt oder abschließend verworfen wird.

## Was niemals im Klartext geloggt werden darf

Nicht loggen:

- Passwörter, Access-/Refresh-Tokens, Cookies und Session-IDs,
- API-Schlüssel, private Schlüssel und sonstige Secrets,
- Datenbankpasswörter oder vollständige Connection-Strings,
- Gesundheitsdaten sowie unnötige Klient:innendaten,
- vollständige Request- oder Response-Bodies,
- Zahlungs-, Bank- oder staatliche Identifikationsnummern,
- große Dokument-, Datei- oder Nachrichteninhalte,
- SQL-Statements mit sensiblen Parameterwerten.

Wenn eine Korrelation notwendig ist, sind Werte zu maskieren, zu pseudonymisieren oder gezielt zu hashen. Maskierung muss an der Quelle erfolgen, nicht erst in der UI. Lognachrichten aus Benutzereingaben sind gegen Log-Injection zu sanitizen; Zeilenumbrüche und Steuerzeichen dürfen keine zusätzlichen Logeinträge erzeugen.

## Audit- und Security-Logs

Audit- und Security-Logs sind fachlich von technischen Diagnose-Logs zu trennen. Sie müssen:

- manipulationsgeschützt und zugriffsbeschränkt gespeichert werden,
- länger und nachvollziehbar nach den geltenden Anforderungen aufbewahrt werden,
- den Zugriff auf die Logs selbst erfassen,
- nicht über eine allgemeine technische „Alle Logs löschen“-Funktion löschbar sein.

Die Admin-Ansicht darf technische Diagnose-Logs filtern und exportieren. Löschen oder Zurücksetzen von Auditdaten braucht eine eigene Berechtigung, eine Begründung und ein weiteres Audit-Ereignis.

## Speicherung, Rotation und Betrieb

- Technische Logs werden täglich rotiert und mit Größen- bzw. Aufbewahrungsgrenzen versehen.
- Das System muss gegen Log-Fluten und volllaufende Datenträger geschützt sein.
- Logdateien und Exporte liegen außerhalb öffentlich erreichbarer Webpfade.
- Zugriffe sind rollenbasiert, insbesondere die aktuelle OpenFLS-Logverwaltung bleibt auf Admins beschränkt.
- Exporte und Backups unterliegen denselben Schutz- und Aufbewahrungsregeln wie die Quelle.
- Löschfristen werden fachlich, datenschutzrechtlich und betrieblich festgelegt; Logs dürfen weder zu früh noch unbegrenzt aufbewahrt werden.

## Spring- und Angular-Regeln

- Backend-Logs verwenden SLF4J/Logback und strukturierte Parameter statt String-Konkatenation.
- Ereignisse werden im Backend an der fachlich verantwortlichen Service-Grenze geloggt; Controller loggen keine vollständigen Nutzdaten.
- Der globale Log-Level ist standardmäßig ausreichend für Betrieb und Security. `DEBUG`/`TRACE` sind gezielte Diagnoseoptionen.
- Frontend-Logs enthalten keine Klient:innendaten, Tokens oder vollständigen API-Payloads. Erwartete Benutzerfehler gehören in die UI und nicht automatisch in `ERROR`.
- WebSocket-, Export- und Admin-Aktionen werden mit Correlation-ID und Ergebnis protokolliert.
- Neue Logging-Verträge werden als DTOs bzw. strukturierte Ereignisse dokumentiert; JPA-Entities werden nicht in Lognachrichten serialisiert.

## Review-Checkliste

Vor dem Merge prüfen:

- Ist der Zweck des Ereignisses klar?
- Sind Zeitpunkt, Quelle, Akteur, Ziel und Ergebnis erkennbar?
- Sind Correlation-/Trace-IDs vorhanden?
- Sind Secrets und personenbezogene Daten ausgeschlossen oder pseudonymisiert?
- Ist das Log-Level angemessen?
- Wird ein Audit-Ereignis benötigt?
- Gibt es Rate-Limit-, Rotations- und Aufbewahrungsfolgen?
- Ist die Änderung in Backend, Frontend und gegebenenfalls Export/API-Vertrag konsistent?

## Referenzen

- [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html)
- [NIST SP 800-92 – Guide to Computer Security Log Management](https://nvlpubs.nist.gov/nistpubs/legacy/SP/nistspecialpublication800-92.Pdf)
- [OpenTelemetry Logs Data Model](https://opentelemetry.io/docs/specs/otel/logs/data-model/)
