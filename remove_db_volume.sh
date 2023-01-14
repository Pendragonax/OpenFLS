#!/bin/bash

echo -e "\e[31mDo you realy want to delete the database?\e[0m"
echo -n -e "\e[31mType in 'delete' to confirm your request: \e[0m"

read answer

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
