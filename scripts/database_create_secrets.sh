#!/bin/bash

DIR="../secrets"

if ! [ -d "$DIR"]; then
	mkdir $DIR
fi

echo -e "\e[36m[INFO] create default secrets at ./secrets\e[0m"
echo -n "user_password" > $DIR/db_password.secret
echo -n "password" > $DIR/db_root_password.secret
echo -n "user" > $DIR/db_user.secret

echo -e "\e[36m[INFO] make sure to change the content of the secret files. Dont use the default values in production!\e[0m"
echo -e "\e[36m[INFO] secrets created.\e[0m"