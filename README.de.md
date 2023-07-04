[![Linux](https://svgshare.com/i/Zhy.svg)](https://svgshare.com/i/Zhy.svg)
[![GitHub release](https://img.shields.io/badge/version-0.3.5-blue)](https://GitHub.com/Pendragonax/OpenFLS/releases/)
[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

# OpenFLS
Lesen Sie das in einer anderen Sprache: [german](https://github.com/Pendragonax/OpenFLS/blob/master/README.de.md), [english](https://github.com/Pendragonax/OpenFLS/blob/master/README.md)

OpenFLS ist eine multi-container-Anwendung für die klientenbezogene Dokumentation, Kontrolle und Auswertung von Arbeitszeiten, welche sich gerade an soziale Einrichtungen im Bundesland Hessen (Deutschland) orientiert.
Dabei stellt diese eine openSource Alternative zu bestehenden proprietären Software und soll die Digitalisierung in diesem Bereich voranbringen und kleinere bis mittlere Unternehmen dabei unterstützen.
Hauptziel ist die Unabhängigkeit von Software Unternehmen und die damit einhergehende Selbstkontrolle und Selbstverwaltung.
Für die Anforderungsermittlung wurde eine Einrichtung und der alte sowie neue Rahmenvertrag des Landes Hessen herangezogen.

## Voraussetzungen
Für die Installtion ist eine Linux-Distribution notwendig sowie die für die Containerisierung notwendigen Pakete *docker* und *docker-compose*.

## Installation
Um diese Anwendung zu installieren, klonen sie dieses Repository und starten Sie das script *initialize.sh*.
In diesem werden die Standard Zugangsdaten erstellt und die für die Verschlüsselung der *tokens* notwendigen Schlüssel.

``` console
/bin/bash initialize
# oder
./initialize.sh
```

Stellen sie sicher, dass Sie die Zugangsdaten im Ordner *secrets* ändern (achten sie darauf keine leere Zeile am Ende zu belassen).
In der Datei *.env* können sie noch weitere Konfigurationen tätigen wie dem Pfad zum Zertifikat, dem weitergeleiteten Port etc.
Um die Anwendung zu starten, nutzen sie folgenden Befehl:

``` console
# DEVELOPMENT-MODE
docker-compose up
# PRODUCTION-MODE
docker-compose -f docker-compose.prod.yml up
```

## Betrieb
Bei einer frischen Installation erreichen sie unter *localhost:PORT* (Port ist standardmäßig 8000) die Anwendung.
Sollte kein Nutzer vorhanden sein, dann ist der Login mit *admin* und *admin* möglich.
Anschließend sollten sie unter dem Menüpunkt *Mitarbeiter* einen neuen Administrator erstellen.
Dabei ist die zugewiesene Rolle als **Adminstrator** unumgänglich, da sonst die Datenbank zurückgesetzt werden muss.
Bei der Erstellung eines Mitarbeiters ist das Passwort initial gleich dem Benutzernamen.
Im weiteren Verlauf kann dieses jederzeit durch den Nutzer geändert werden.

## Skripte
WiP

## Backups
WiP