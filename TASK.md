# TASK.md – Aktuelle Aufgabe

## 1. Aufgabe
Kurzbeschreibung der Aufgabe:
- Backend-Build von Maven auf Gradle umstellen.

Ziel der Aufgabe (Outcome, nicht nur Output):
- Gradle-basierter Build mit identischen Abhaengigkeiten/Plugins wie im aktuellen Maven `pom.xml`.

Prioritaet:
- Mittel

Deadline / Zeitrahmen (falls relevant):
- —

## 2. Kontext fuer diese Aufgabe
Relevante Infos aus DOMAIN und STATE:
- Backend nutzt Spring Boot 3.3.2, Kotlin, Java 17, Flyway, JPA, Security.
- Aktuelle Build-Definition in `backend/pom.xml` mit Kotlin Maven Plugin und Spring Boot Maven Plugin.

## 3. Erwarteter Output
Formate / Artefakte, die am Ende vorliegen sollen:
- `backend/build.gradle.kts` und `backend/settings.gradle.kts` mit korrekten Plugins/Dependencies.
- Entfernte/ersetzte Maven Wrapper und `pom.xml` (oder klar dokumentiert, wenn parallel behalten).
- Angepasste Build/Run-Anweisungen in Doku und ggf. Dockerfile/CI.

## 4. Vorschlag fuer Ablauf (optional)
- Schritt 1: Plan
- Schritt 2: Build
- Schritt 3: Critic
- Schritt 4: Refactor
