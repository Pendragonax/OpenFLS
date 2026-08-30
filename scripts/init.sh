#!/bin/bash
# Interaktiver Einstiegspunkt für die lokale Erstinitialisierung.

echo "Welcome to OpenFls!"
echo "This script will run all sub-scripts to initialize the necessary parameters."
echo -n "To proceed type in 'go': "

read input

# Führt die Unter-Skripte nur nach ausdrücklicher Bestätigung aus.
if [ "$input" = "go" ]; then
	script/database_create_secrets.sh
	script/backend_generate_rsa_keys.sh
else
    echo -e "\e[31maborted\e[0m"
fi
