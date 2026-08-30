#!/bin/bash
# Legt lokale Standard-Secret-Dateien für die Entwicklung an.

# Der Ordner liegt außerhalb des Docker-Verzeichnisses und wird nicht versioniert.
DIR="../secrets"

# Erstellt den Zielordner nur, wenn er noch nicht vorhanden ist.
if ! [ -d "$DIR"]; then
	mkdir $DIR
fi

# Schreibt bewusst unsichere Platzhalter, die vor einem Produktivbetrieb ersetzt werden müssen.
echo -e "\e[36m[INFO] create default secrets at ./secrets\e[0m"
echo -n "user_password" > $DIR/db_password.secret
echo -n "password" > $DIR/db_root_password.secret
echo -n "user" > $DIR/db_user.secret

echo -e "\e[36m[INFO] make sure to change the content of the secret files. Dont use the default values in production!\e[0m"
echo -e "\e[36m[INFO] secrets created.\e[0m"
