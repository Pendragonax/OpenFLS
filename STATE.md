# STATE.md – Aktueller Projektzustand

[STATE]
Projekt:
- Name: OpenFLS
- Kurzstatus (1–2 Sätze): Frontend-Dev-API zeigt auf lokales Backend; Compose-Dateien und .env sind in docker/ umgezogen und Pfade/Docs angepasst.

Aktueller Vibe Mode:
- Co-Creation

Aktueller Fokus:
- Lokale Dev-Setup-Verbesserungen und Compose-Ordnerstruktur.

Bisherige Entscheidungen:
- `frontend/src/environments/environment.ts` nutzt `http://localhost:8081/api/` fuer lokale Entwicklung.
- Alle Compose-Dateien und `docker-compose.env` liegen unter `docker/` mit angepassten relativen Pfaden.
- `.env` liegt nun unter `docker/.env` und Compose-Aufrufe nutzen `--env-file`.

Bereits erledigt:
- Frontend-Dev-API-URL aktualisiert.
- Compose-Dateien verschoben und Pfade aktualisiert; READMEs angepasst.
- `.env` nach `docker/.env` verschoben; README-Kommandos aktualisiert.

Offene Punkte / Fragen:
- Falls CORS benoetigt wird, Backend pruefen/anpassen.
- Optional: Proxy-Konfiguration fuer `ng serve` statt absoluter API-URL.

Nächste geplante Schritte:
- Compose-Setup mit `docker-compose --env-file docker/.env -f docker/docker-compose.yml up` verifizieren.
- Laufende Dev-Workflows feinjustieren (Proxy/CORS je nach Bedarf).
