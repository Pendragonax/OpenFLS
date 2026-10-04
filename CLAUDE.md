# CLAUDE.md

Die verbindlichen Projektregeln stehen in `AGENTS.md`. Diese Datei importiert sie
unverändert, damit es nur eine Quelle gibt, und ergänzt Claude-Code-spezifische
Hinweise.

@AGENTS.md

## Claude-Code-spezifische Hinweise

- **Verbindliche Backend-Prüfung:** Nach jeder Backend-Änderung zusätzlich zu
  gezielten Tests immer `./gradlew clean build` im Verzeichnis `backend/`
  ausführen. Die Arbeit gilt erst als geprüft, wenn dieser Clean-Build fehlerfrei
  durchläuft (siehe `AGENTS.md` → „Verbindliche Backend-Prüfung“).
- **Backend-Architektur (Domänen-Migration):** Es gelten die AGENTS.md-Abschnitte
  „Service-Arten“, „Entity-Weitergabe zwischen Services“, „Projections“ und
  „DTO-Regeln“. Kurzform: CRUD-Services und Anwendungsfall-Services sind getrennte
  Klassen; Entity-Methoden zwischen Services tragen `@InternalEntityApi` und
  dürfen nie von RestControllern genutzt werden (ArchTest); Projections bleiben
  zwischen Repository und Service; HTTP-Grenz-DTOs heißen `…Request`/`…Response`
  (mit Anwendungsfall, z. B. `AbsenceCreateRequest`), service-interne DTOs nicht;
  `Solo`/`Simple`/`XL` entfallen zugunsten von `XDto` und `XWithY`.
  Erwartete Fälle (z. B. „nicht gefunden“) im Controller als Response
  zurückgeben, nicht per Exception; Projections gehören der Domäne, die sie
  abfragt (ungenutzte in der eigenen Domäne dort anlegen, wo sie gebraucht werden).
  Kein toter Code: keine ungenutzten Methoden/Klassen/Properties und keine
  Member, die nur wegen Tests existieren (bei Migration jeder Domäne prüfen).
  Fachliche Prüfungen (Existenz, Wertebereiche, Konflikte) gehören in den
  Service, nicht in den Controller; bei mehreren erwarteten Ausgängen gibt
  die Service-Methode eine eigene `sealed class` zurück (z. B.
  `HourCorridorUpdateResult`), der Controller übersetzt sie per `when`. Gilt
  auch für `delete(id)` (`XDeleteResult`) statt Vorab-`getById`/`existsById`
  im Controller. Performance-Logging: `startMs` immer als erste Zeile, vor
  jeder Prüfung und außerhalb `try`/`catch`; Logging im `finally`.
  Arbeitsmodus pro Domäne: Inventur → Vorschlag → Freigabe → Umsetzung.
- **Logging:** Für alle Änderungen am Logging gilt `docs/logging-guide.md`.
- **Skills:** Projekt-Skills liegen unter `.claude/skills/` (portiert aus
  `.codex/skills/`): `build`, `mockup`, `local-fix-test-coverage`.
- **MCP-Server:** In `.mcp.json` definiert – Spring-Docs, Angular-Docs (Context7),
  Playwright. Bevorzugt die offiziellen Doku-Quellen für Spring Boot / Spring
  Data / Spring Security / Angular. Playwright bzw. Browser-Automatisierung nur
  gegen ausdrücklich freigegebene Testumgebungen verwenden.
- **Frontend:** Angular ist die verbindliche Technologie. React erst bei einer
  ausdrücklich beauftragten Migration.
- **Antwortstil (Chat):** Chat-Antworten kompakt halten, keine Prosa-Einleitungen
  oder -Zusammenfassungen. Gilt nur für Chat-Text, nicht für Commit-Messages oder
  PR-Beschreibungen.
  - Kein "Great question!", keine Wiederholung der Aufgabe, kein Fazit am Ende,
    außer explizit gefragt.
  - Ergebnis direkt nennen: was geändert wurde, wo, was offen ist — sonst nichts.
  - Zwischenstatus nur an wichtigen Punkten, max. 1 Satz.
  - Bei Rückfragen: Frage stellen statt Optionen ausbreiten.
