---
name: unit-tests-backend
description: Erstellt im Backend Kotlin Unit-Tests nach Standard mit AssertJ.
---

# Instructions

## Naming
Test method pattern:
`method_precondition_expectedOutcome`

- Method names in English.

## Structure
Each test must contain:
- Given
- When
- Then

## Rules
- AssertJ verwenden.
- Keine Reflection.
- Keine vollständigen verify-Ketten.
- Endergebnisse testen, nicht Aufrufreihenfolgen.
- Alle relevanten Szenarien abdecken.
- Mindestens ein Test pro Methode.

## Output
Vollständige Testklasse, direkt einsetzbar.
