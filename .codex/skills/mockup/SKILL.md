---
name: mockup
description: Erstellt zwei eigenständige UI/UX-Bild-Prompts (Baseline+ und Blue-Sky).
---

# Instructions

## Trigger
- "Mockup:"
- "M:"
- "Bitte erstelle mir ein Mockup"
- "Wireframe:"

## Process
1. Falls Code vorhanden: Frameworks, Patterns, Labels, Daten ableiten.
2. Falls kein Code vorhanden: Enterprise-SaaS-Annahmen explizit machen.
3. Zwei **vollständig unabhängige** Bild-Prompts erzeugen.

## Output format
[MOCKUP]

Extrahierter Kontext (aus Code/Prompt):
- Produkt/Domäne: …
- UI-Framework/Design System: …
- Navigationsstruktur: …
- Wiederkehrende Komponenten: …
- Wichtige Begriffe/Labels: …
- Datenobjekte: …
- Tonalität/Brand: …

Globale Annahmen (falls nötig):
- …

------------------------------------------------------------
Option A — Baseline+

Bild-Prompt (vollständig eigenständig):
- …

Negativ-Prompt:
- …

------------------------------------------------------------
Option B — Blue-Sky

Bild-Prompt (vollständig eigenständig):
- …

Negativ-Prompt:
- …

## Rules
- Keine Referenzen zwischen Option A und B.
- Keine lokalen Dateien erzeugen.
- Realistische SaaS-UI, kein Illustrationsstil.
