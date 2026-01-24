# TASK.md – Aktuelle Aufgabe

## 1. Aufgabe
Kurzbeschreibung der Aufgabe:
- Dockerfile so anpassen, dass beim Build RSA-Keys per Script generiert werden.

Ziel der Aufgabe (Outcome, nicht nur Output):
- Build erzeugt `backend/src/main/resources/private.key` und `public.key` reproduzierbar vor dem JAR-Build.

Prioritaet:
- Mittel

Deadline / Zeitrahmen (falls relevant):
- —

## 2. Kontext fuer diese Aufgabe
Relevante Infos aus DOMAIN und STATE:
- Script: `scripts/backend_generate_rsa_keys.sh`.
- Backend-Dockerfile nutzt Gradle Build-Stage.

## 3. Erwarteter Output
Formate / Artefakte, die am Ende vorliegen sollen:
- Angepasste `backend/Dockerfile` (ggf. Script- oder Build-Stage angepasst).
- Falls noetig: Script oder Pfade angepasst, damit Keys korrekt landen.

## 4. Vorschlag fuer Ablauf (optional)
- Schritt 1: Plan
- Schritt 2: Build
- Schritt 3: Critic
- Schritt 4: Refactor
