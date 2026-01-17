#!/bin/bash

echo -e "\e[36m[INFO] start the backup-process to /var/open-fls/backup/<date>-openfls-backup.tar.bz2\e[0m"
sudo docker run --rm -v openfls_open-fls-db:/volume -v /var/open-fls/backup:/backup loomchild/volume-backup backup $(date +%F)-openfls-backup

echo -e "\e[36m[INFO] finished\e[0m"
