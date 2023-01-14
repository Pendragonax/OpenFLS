#!/bin/bash

echo -e "\e[36m[INFO] create default secrets at ./secrets\e[0m"
echo -n "user_password" > secrets/db_password.secret
echo -n "password" > secrets/db_root_password.secret
echo -n "user" > secrets/db_user.secret

echo -e "\e[36m[INFO] make sure to change the content of the secret files. Dont use the default values in production!\e[0m"
echo -e "\e[36m[INFO] secrets created.\e[0m"