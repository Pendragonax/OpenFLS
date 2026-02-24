# Version 3.0.3

**Release-Datum:** 2026-02-23

# Highlights

- Möglichkeit, Abwesenheitstage einzutragen und somit eine korrekte Kontingentauslastung zu bestimmen
- Überarbeitete Kontingentauslastungsanzeige mit detaillierten Informationen zu Abwesenheiten und Restkontingenten
- Erstellung/Bearbeitung einer Dokumentation auf einer Seite mit zusätzlichen Informationen
- Erstellung/Bearbeitung eines Hilfeplans auf einer Seite mit zusätzlichen Informationen
- Ansicht der Hilfepläne überarbeitet und mit mehr Informationen versehen
- Kontingentansicht in einem Bereich überarbeitet
- Aktuelle Versionen eingepflegt

# Abwesenheitstage

![Abwesenheitstage](/api/changelog/assets/hour-view-1.png)

Über den "Home"-Bereich können nun Abwesenheitstage eingetragen werden, um die Kontingentauslastung korrekt zu bestimmen.  
Es können verschiedene Arten von Abwesenheiten eingetragen werden, wie z. B. Urlaub, Krankheit oder andere Gründe.  
Wochenenden oder Feiertage werden automatisch als Abwesenheitstage berücksichtigt, um eine realistische Kontingentauslastung zu gewährleisten.  
Diese dürfen zwingend nicht als Abwesenheitstage eingetragen werden, da sie sonst die Kontingentauslastung verfälschen würden.

![Abwesenheitstage ein-/austragen](/api/changelog/assets/hour-view-2.png)

Die Abwesenheitstage können über einen Linksklick auf den entsprechenden Tag eingetragen werden.  
Es öffnet sich ein Dialog, in dem die Abwesenheit angegeben werden kann.  
In diesem Dialog kann die Abwesenheit auch wieder entfernt werden, falls sie nicht mehr relevant ist.  
Weiterhin kann über diesen Kalender auf die eigenen Dokumentationen für den entsprechenden Tag navigiert werden.  
Die Legende erläutert die verschiedenen Arten der farblichen Markierungen, um die Übersicht zu erleichtern.

# Kontingentauslastungsanzeige

![Kontingentauslastung](/api/changelog/assets/hour-view-3.png)

Die Kontingentauslastung wird nun mit detaillierten Informationen zu Restkontingenten angezeigt.  
Dabei gilt die Anzeige jeweils vom Beginn des **Tages**, der **Woche** und des **Monats** bis einschließlich des heutigen Tages.  
Die Anzeige berücksichtigt die eingetragenen Abwesenheitstage, um eine realistische Darstellung der Kontingentauslastung zu gewährleisten.

![Kontingentauslastung - mobil](/api/changelog/assets/hour-view-mobile.png)

# Home

![Home Ansicht](/api/changelog/assets/home.png)

Die **Home**-Ansicht wurde überarbeitet und zeigt jetzt detailliertere Informationen zu den favorisierten Hilfeplänen an.

![Home Ansicht- mobil](/api/changelog/assets/home-mobile.png)

Der Home-Bereich wurde für die mobile Ansicht überarbeitet, um eine bessere Benutzererfahrung auf mobilen Geräten zu bieten.

# Erstellung/Bearbeitung einer Dokumentation

![Dokumentation bearbeiten 1/2](/api/changelog/assets/service-new-1.png)

Die Erstellung und Bearbeitung einer Dokumentation wurde überarbeitet, um eine einfachere und intuitivere Benutzeroberfläche zu bieten.  
Es befindet sich nun alles auf einer Seite, und es können zusätzliche Informationen angezeigt werden.  
Weiterhin werden die Klienteneinträge des ausgewählten Datums angezeigt, um die Übersicht zu verbessern und mögliche Konflikte bei der Planung zu vermeiden.  
Bei der Auswahl des Hilfeplans wird der dazugehörige Bereich dargestellt.

![Dokumentation bearbeiten 1-2](/api/changelog/assets/service-new-3.png)  
Die Eingabe der Zeit kann jetzt zwischen **Uhrzeit von/bis** oder **Dauer** gewählt werden, um die Flexibilität bei der Zeiteingabe zu erhöhen.

![Dokumentation bearbeiten 2-2](/api/changelog/assets/service-new-2.png)

Für die Übersicht über die zeitlichen Konditionen eines Hilfeplans gibt es die Hilfeplaninformation.  
In dieser werden die übrigen Stunden angezeigt, die in dieser **Woche**, diesem **Monat** und diesem **Kalenderjahr** bestehen.  
Dabei ist wichtig zu berücksichtigen, dass nur genau dieser Zeitraum betrachtet wird und nicht das **Davor** oder **Danach**.  
Am Ende gibt es noch eine kleine Übersicht über die zu tätigenden Eingaben, damit ein Eintrag gespeichert werden kann.

# Erstellung/Bearbeitung eines Hilfeplans

![Hilfeplan bearbeiten](/api/changelog/assets/assistance-plan-new-1.png)

Die Erstellung und Bearbeitung eines Hilfeplans wurde überarbeitet, um eine einfachere und intuitivere Benutzeroberfläche zu bieten.  
Es befindet sich nun alles auf einer Seite, und es können zusätzliche Informationen angezeigt werden.  
Weiterhin werden Hilfepläne des Klienten angezeigt, sodass Überschneidungen bei der Planung vermieden werden können.  
Wenn eine solche Überschneidung festgestellt wird, färbt sich der Hilfeplan gelb ein.

![Hilfeplan bearbeiten](/api/changelog/assets/assistance-plan-new-2.png)

Ab sofort können Stunden entweder direkt beim Hilfeplan oder bei den Zielen hinterlegt werden.  
Die Kombination aus beidem ist somit nicht mehr erlaubt.

![Hilfeplan Stunden](/api/changelog/assets/assistance-plan-new-3.png)

Die Eintragung der Stunden wurde von "Stunde, Anteil einer Stunde" auf "Stunde, Minute" umgestellt.  
Alle vorhandenen Werte wurden übertragen und sollten noch einmal auf Korrektheit überprüft werden.

![Hilfeplan - mobile](/api/changelog/assets/assistance-plan-new-mobile.png)

# Hilfeplanansicht

Die Ansicht der Hilfepläne sowie der Favoriten wurde überarbeitet.

![Hilfeplan - Favoriten](/api/changelog/assets/assistance-plan-view-1.png)  
![Hilfeplan - Aktiv](/api/changelog/assets/assistance-plan-view-2.png)

Hier sind zwei neue Spalten hinzugekommen.  
Die Spalte "Status" zeigt an, ob der Hilfeplan aktuell aktiv ist.  
Sollte die Spalte leer sein, liegt dieser in der Zukunft oder der Vergangenheit.

![Hilfeplan - Erfüllt](/api/changelog/assets/assistance-plan-view-3.png)

In der Spalte "Erfüllt dieses Jahr bis jetzt" wird ein Statusbalken angezeigt, der die geleisteten Stunden eines Hilfeplans (links) mit den genehmigten Stunden (rechts) darstellt.  
Dabei handelt es sich nur um eine relativ ungenaue Anzeige, da hier alle Stundentypen (zur Vereinfachung) zusammengefasst werden sowie alle geleisteten Stunden unterschiedlicher Stundentypen.  
Diese Anzeige dient der "schnellen" Ersteinschätzung des aktuellen Stands des Hilfeplans.  
Berechnet werden alle geleisteten und bewilligten Stunden des aktuellen Kalenderjahres bis einschließlich des heutigen Tages.

![Hilfeplan - mobil](/api/changelog/assets/assistance-plan-view-mobile.png)

# Kontingentübersicht

![Kontingentübersicht](/api/changelog/assets/contingent-overview-1.png)

Für Leitungskräfte steht die Kontingentübersicht in einem Bereich zur Verfügung.  
Diese wurde optisch überarbeitet und berücksichtigt die eingetragenen Abwesenheitstage.  
Der Wert "Geleistet [%]" sollte somit den realistischen Wert widerspiegeln.

![Kontingentübersicht - Abwesenheitstage](/api/changelog/assets/contingent-overview-2.png)

Die Abwesenheitstage der Mitarbeiter sind ebenfalls in dieser Übersicht visualisiert.