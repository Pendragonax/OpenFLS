#!/bin/bash

echo "Welcome to OpenFls!"
echo "This script will run all sub-scripts to initialize the necessary parameters."
echo -n "To proceed type in 'go': "

read input

if [ "$input" = "go" ]; then
	/bin/bash create_database_secrets.sh
	/bin/bash generate_backend_rsa_keys.sh
else
    echo -e "\e[31maborted\e[0m"
fi