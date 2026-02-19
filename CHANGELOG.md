# Changelog

All notable changes to this project will be documented in this file.


## 3.0.0

#### Features
- add absence days and add to calculations
- add multiple mobile views
- add single page service creation and editing
- add single page assistance plan creation and editing
- add backup service with retention policy
- add docker images instead of building local images
- refactor restore database script
- refactor contingent view
- refactor service view
- refactor home view
- refactor hour view
- refactor assistance plan view with multiple new statistics
- switch assistance plans hours to HH,MM format
- switch from maven to gradle
- update backend versions
- update frontend versions

#### Bug
- fix several bugs
- refactor multiple backend services


## 2.0.1

#### Bug
- fix several merge conflicts problems


## 2.0.0

#### Features
- add flyway for database migrations
- group services
- group service evaluation

#### Bug
- fix several overview bugs
- fix bug by changing roles


## 1.3.1

#### Bug
- fix service auto increment


## 1.3.0

#### Features
- add client filter
- add assistance plan analysis
- add illegal documentation and assistance plan option
- enable modifying services without time constraints


## 1.2.0

#### Features
- new service table pages for the logged in user and complete
- contingent overview for the logged in user
- contingent overview routes to the selected date


## 1.1.1

#### Bug
- assistance plan analysis doesnt recognize sponsor


## 1.1.0

#### Features
- assistance plan analysis
- contigent analysis by institution
- minutes in employee contigent overview



## 1.0.0

#### Features
- Evaluations for each goal
- Goal time evaluation for each goal
- Favorite assistance plans
- perfomance logs


## 0.4.2

#### Bug

- Navbar shrinks on mobile view
- Service filter delayed on multiple selectes


## 0.4.1

#### Bug

- Bug when editing an service because of removeing the endDate


## 0.4.0

#### Feature

- Add assistanceplan overview for executed, approved and difference hours
- Remove end date control from new service
- Fix auto logout when token expired

## 0.3.6

#### Feature

- Add service filter
- Rework service layout
- Show goals on service detail

## 0.3.5

#### Feature

- Grant admin right to add assistance plans for all institutions
- Grant admin right to add assistance goals for everyone
- Remove contingent from home page

## 0.3.4

#### Fix

- Fix: Admin can add assistance plans for all

## 0.3.3

#### Fix

- Fix: Export serivce hours crashed

## 0.3.2

#### Fix

- Fix: Admin cant select all instituions at creating contingents

## 0.3.1

#### Fix

- Fix: Charcters illegal at output
- Fix: Evaluation tables incorrect calculation

## 0.3.0

#### Features

- evaluation of assistance plans
- show start and end of each service
- filter hour type by assistance plan when creating and assistance entry
- add 14 day period to edit and delete own assistance entries
- add given date url when creating an assistance entry
- use date of the selected overview when creating an entry
- add related assistance entries to the delete confirmations

#### Fix

- Fix: sort categories when creating an assistance entry
- Fix: proxy restart now working
- Fix: no confirmation when deleting a goal

## 0.2.0

#### Features

- contingent evalution for employees and leader
- export tables for all assistance plans
- add ```file-saver``` version ```2.0.5```

#### Fix

- Fix: check and add folder in create secrets script
- Fix: contingent end before start in frontend possible
- Fix: service end before start in frontend possible
- Fix: assistance plan sum of all hours bug
- Fix: service permission on getMapping bug
- Fix: loading states will not refresh when automatically logged out
- Fix: sorting of different entites in the backend