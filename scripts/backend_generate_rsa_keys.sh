#!/bin/bash

echo -e "\e[36m[INFO] generate temporary keypair\e[0m"
openssl genrsa -out keypair.pem 2048

echo -e "\e[36m[INFO] generate temporary public-key\e[0m"
openssl rsa -in keypair.pem -pubout -out public.key

echo -e "\e[36m[INFO] generate temporary private-key\e[0m"
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.key

echo -e "\e[36m[INFO] copy private-key to ../backend/src/main/resources/private.key\e[0m"
cp private.key ../backend/src/main/resources/private.key

echo -e "\e[36m[INFO] copy public-key to ../backend/src/main/resources/public.key\e[0m"
cp public.key ../backend/src/main/resources/public.key

echo -e "\e[36m[INFO] remove temporary generated keys\e[0m"
rm private.key
rm public.key
rm keypair.pem

echo -e "\e[36m[INFO] generated backend keys\e[0m"