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
- **Logging:** Für alle Änderungen am Logging gilt `docs/logging-guide.md`.
- **Skills:** Projekt-Skills liegen unter `.claude/skills/` (portiert aus
  `.codex/skills/`): `build`, `mockup`, `local-fix-test-coverage`.
- **MCP-Server:** In `.mcp.json` definiert – Spring-Docs, Angular-Docs (Context7),
  Playwright. Bevorzugt die offiziellen Doku-Quellen für Spring Boot / Spring
  Data / Spring Security / Angular. Playwright bzw. Browser-Automatisierung nur
  gegen ausdrücklich freigegebene Testumgebungen verwenden.
- **Frontend:** Angular ist die verbindliche Technologie. React erst bei einer
  ausdrücklich beauftragten Migration.
