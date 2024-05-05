[![GitHub release](https://img.shields.io/badge/version-1.1.0-blue)](https://GitHub.com/Pendragonax/OpenFLS/releases/)
[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

# OpenFLS
Read this in other languages: [german](https://github.com/Pendragonax/OpenFLS/blob/master/README.de.md), [english](https://github.com/Pendragonax/OpenFLS/blob/master/README.md)

OpenFLS is a multi-container application for client-related documentation, control and evaluation of working hours, which is oriented towards social institutions in the federal state of Hesse (Germany).
It is an open source alternative to existing proprietary software and is intended to promote digitization in this area and support small to medium-sized enterprises.
The main goal is the independence from software companies and the accompanying self-control and self-administration.
For the determination of requirements, an institution and the old and new framework agreement of the state of Hesse were used.

## Requirements
For the installation a linux distribution is necessary as well as the packages *docker* and *docker-compose* necessary for the containerization.

## Install
To install this application, clone this repository and run the script *initialize.sh*.
This will create the default credentials and the keys needed to encrypt the tokens.

``` console
/bin/bash initialize
# OR
./initialize.sh
```

Make sure you change the credentials in the *secrets* folder (be sure not to leave an empty line at the end).
In the *.env* file you can make further configurations like the path to the certificate, the forwarded port, etc.
To start the application use the following command:

``` console
# DEVELOPMENT-MODE
docker-compose up
# PRODUCTION-MODE
docker-compose -f docker-compose.prod.yml up
```

## Operation
With a fresh installation you can reach the application under *localhost:PORT* (port is 8000 by default).
If there is no user, then the login with *admin* and *admin* is possible.
Then they should create a new administrator under the menu item *Mitarbeiter*.
Thereby the assigned role as **adminstrator** is essential, because otherwise the database has to be reset.
When creating an employee, the password is initially the same as the user name.
In the further course this can be changed at any time by the user.

## Scripts
WiP

## Backups
WiP