#!/bin/bash

if test -f "$1"; then
	echo -e "\e[31mDo you want to restore the backup database?\e[0m"
	echo -n -e "\e[31mType in 'restore' to confirm your request: \e[0m"

	read answer

	if [ "$answer" = "restore" ]; then
		echo -e "\e[36m[INFO] restore database from backup $1\e[0m"
		sudo docker run -i -v openfls_open-fls-db:/volume --rm loomchild/volume-backup restore -f < $1

		echo -e "\e[36m[INFO] finished $1\e[0m"
	fi
else 
    echo -e "\e[31m$1 does not exist!\e[0m"
fi
