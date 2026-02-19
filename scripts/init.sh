#!/bin/bash

echo "Welcome to OpenFls!"
echo "This script will run all sub-scripts to initialize the necessary parameters."
echo -n "To proceed type in 'go': "

read input

if [ "$input" = "go" ]; then
	script/database_create_secrets.sh
	script/backend_generate_rsa_keys.sh
else
    echo -e "\e[31maborted\e[0m"
fi