#!/bin/bash
# Entfernt nach expliziter Bestätigung den lokalen Datenbank-Container und sein Volume.

echo -e "\e[31mDo you realy want to delete the database?\e[0m"
echo -n -e "\e[31mType in 'delete' to confirm your request: \e[0m"

# Wartet auf die eindeutige Bestätigung, bevor irreversible Docker-Befehle ausgeführt werden.
read answer

# Stoppt zuerst den Container, entfernt ihn und löscht erst zuletzt das Daten-Volume.
if [ "$answer" = "delete" ]; then
	echo -e "\e[36m[INFO] stop the open-fls-database container\e[0m"
	sudo docker stop open_fls_db

	echo -e "\e[36m[INFO] remove the open-fls-database container\e[0m"
	sudo docker rm open_fls_db

	echo -e "\e[36m[INFO] remove the open-fls-database volume\e[0m"
	sudo docker volume rm openfls_open-fls-db
else
	echo -e "\e[36m[INFO] aborted\e[0m"
fi
