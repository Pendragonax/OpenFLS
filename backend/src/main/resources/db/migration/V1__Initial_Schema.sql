CREATE TABLE `sponsors`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `name`         varchar(32) DEFAULT NULL,
    `pay_exact`    bit(1) NOT NULL,
    `pay_overhang` bit(1) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `institutions`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `email`       varchar(64) DEFAULT NULL,
    `name`        varchar(64) DEFAULT NULL,
    `phonenumber` varchar(32) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `category_templates`
(
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `description`    varchar(1024) DEFAULT NULL,
    `title`          varchar(64)   DEFAULT NULL,
    `without_client` bit(1) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `employees`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `description` varchar(1024) DEFAULT NULL,
    `email`       varchar(64)   DEFAULT NULL,
    `firstname`   varchar(64)   DEFAULT NULL,
    `inactive`    bit(1) NOT NULL,
    `lastname`    varchar(64)   DEFAULT NULL,
    `phonenumber` varchar(32)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `hour_types`
(
    `id`    bigint NOT NULL AUTO_INCREMENT,
    `price` double DEFAULT NULL,
    `title` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `unprofessionals`
(
    `employee_id` bigint NOT NULL,
    `sponsor_id`  bigint NOT NULL,
    `end`         date DEFAULT NULL,
    PRIMARY KEY (`employee_id`, `sponsor_id`),
    KEY           `FK_UNPROFESSIONALS_SPONSORS` (`sponsor_id`),
    CONSTRAINT `FK_UNPROFESSIONALS_EMPLOYEES` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
    CONSTRAINT `FK_UNPROFESSIONALS_SPONSORS` FOREIGN KEY (`sponsor_id`) REFERENCES `sponsors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `employees_access`
(
    `id`       bigint NOT NULL,
    `password` varchar(255) DEFAULT NULL,
    `role`     int    NOT NULL,
    `username` varchar(32)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_EMPLOYEE_ACCESS_EMPLOYEE` FOREIGN KEY (`id`) REFERENCES `employees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `permissions`
(
    `employee_id`        bigint NOT NULL,
    `institution_id`     bigint NOT NULL,
    `affiliated`         bit(1) NOT NULL,
    `change_institution` bit(1) NOT NULL,
    `read_entries`       bit(1) NOT NULL,
    `write_entries`      bit(1) NOT NULL,
    PRIMARY KEY (`employee_id`, `institution_id`),
    KEY                  `FK_PERMISSION_INSTITUTION` (`institution_id`),
    CONSTRAINT `FK_PERMISSION_INSTITUTION` FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`),
    CONSTRAINT `FK_PERMISSION_EMPLOYEE` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `categories`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `description`          varchar(1024) DEFAULT NULL,
    `face_to_face`         bit(1) NOT NULL,
    `shortcut`             varchar(8)    DEFAULT NULL,
    `title`                varchar(124)  DEFAULT NULL,
    `category_template_id` bigint        DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                    `FK_CATEGORIES_CATEGORY_TEMPLATES` (`category_template_id`),
    CONSTRAINT `FK_CATEGORIES_CATEGORY_TEMPLATES` FOREIGN KEY (`category_template_id`) REFERENCES `category_templates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `client`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `email`                varchar(64) DEFAULT NULL,
    `first_name`           varchar(64) DEFAULT NULL,
    `last_name`            varchar(64) DEFAULT NULL,
    `phone_number`         varchar(32) DEFAULT NULL,
    `category_template_id` bigint      DEFAULT NULL,
    `institution_id`       bigint      DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                    `FK_CLIENTS_CATEGORY_TEMPLATES` (`category_template_id`),
    KEY                    `FK_CLIENTS_INSTITUTIONS` (`institution_id`),
    CONSTRAINT `FK_CLIENTS_CATEGORY_TEMPLATES` FOREIGN KEY (`category_template_id`) REFERENCES `category_templates` (`id`),
    CONSTRAINT `FK_CLIENTS_INSTITUTIONS` FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `assistance_plans`
(
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `end`            date   DEFAULT NULL,
    `start`          date   DEFAULT NULL,
    `client_id`      bigint DEFAULT NULL,
    `institution_id` bigint DEFAULT NULL,
    `sponsor_id`     bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY              `FK_ASSISTANCE_PLANS_CLIENTS` (`client_id`),
    KEY              `FK_ASSISTANCE_PLANS_INSTITUTIONS` (`institution_id`),
    KEY              `FK_ASSISTANCE_PLANS_SPONSORS` (`sponsor_id`),
    CONSTRAINT `FK_ASSISTANCE_PLANS_SPONSORS` FOREIGN KEY (`sponsor_id`) REFERENCES `sponsors` (`id`),
    CONSTRAINT `FK_ASSISTANCE_PLANS_INSTITUTIONS` FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`),
    CONSTRAINT `FK_ASSISTANCE_PLANS_CLIENTS` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `goals`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `description`        varchar(1024) DEFAULT NULL,
    `title`              varchar(124)  DEFAULT NULL,
    `assistance_plan_id` bigint        DEFAULT NULL,
    `institution_id`     bigint        DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                  `FK_GOALS_ASSISTANCE_PLAN` (`assistance_plan_id`),
    KEY                  `FK_GOALS_INSTITUTIONS` (`institution_id`),
    CONSTRAINT `FK_GOALS_ASSISTANCE_PLAN` FOREIGN KEY (`assistance_plan_id`) REFERENCES `assistance_plans` (`id`),
    CONSTRAINT `FK_GOALS_INSTITUTIONS` FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `goal_hours`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `weekly_hours` double NOT NULL,
    `goal_id`      bigint DEFAULT NULL,
    `hour_type_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY            `FK_GOAL_HOURS_GOALS` (`goal_id`),
    KEY            `FK_GOAL_HOURS_HOUR_TYPES` (`hour_type_id`),
    CONSTRAINT `FK_GOAL_HOURS_GOALS` FOREIGN KEY (`goal_id`) REFERENCES `goals` (`id`),
    CONSTRAINT `FK_GOAL_HOURS_HOUR_TYPES` FOREIGN KEY (`hour_type_id`) REFERENCES `hour_types` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `assistance_plan_favorites`
(
    `employee_id`        bigint NOT NULL,
    `assistance_plan_id` bigint NOT NULL,
    PRIMARY KEY (`employee_id`, `assistance_plan_id`),
    KEY                  `FK_ASSISTANCE_PLAN_FAVORITES_ASSISTANCE_PLAN` (`assistance_plan_id`),
    CONSTRAINT `FK_ASSISTANCE_PLAN_FAVORITES_ASSISTANCE_PLAN` FOREIGN KEY (`assistance_plan_id`) REFERENCES `assistance_plans` (`id`),
    CONSTRAINT `FK_ASSISTANCE_PLAN_FAVORITES_EMPLOYEES` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `assistance_plan_hours`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `weekly_hours` double NOT NULL,
    `assistance_plan_id` bigint DEFAULT NULL,
    `hour_type_id`       bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                  `FK_ASSISTANCE_PLAN_HOURS_ASSISTANCE_PLANS` (`assistance_plan_id`),
    KEY                  `FK_ASSISTANCE_PLAN_HOURS_HOUR_TYPES` (`hour_type_id`),
    CONSTRAINT `FK_ASSISTANCE_PLAN_HOURS_ASSISTANCE_PLANS` FOREIGN KEY (`assistance_plan_id`) REFERENCES `assistance_plans` (`id`),
    CONSTRAINT `FK_ASSISTANCE_PLAN_HOURS_HOUR_TYPES` FOREIGN KEY (`hour_type_id`) REFERENCES `hour_types` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contingents`
(
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `end`            date   DEFAULT NULL,
    `start`          date   NOT NULL,
    `weekly_service_hours` double NOT NULL,
    `employee_id`    bigint DEFAULT NULL,
    `institution_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY              `FK_CONTINGENTS_EMPLOYEES` (`employee_id`),
    KEY              `FK_CONTINGENTS_INSTITUTIONS` (`institution_id`),
    CONSTRAINT `FK_CONTINGENTS_EMPLOYEES` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
    CONSTRAINT `FK_CONTINGENTS_INSTITUTIONS` FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `evaluations`
(
    `id`                     bigint NOT NULL AUTO_INCREMENT,
    `approved`               bit(1) NOT NULL,
    `content`                varchar(1024) DEFAULT NULL,
    `created_at`             datetime(6) DEFAULT NULL,
    `date`                   date          DEFAULT NULL,
    `updated_at`             datetime(6) DEFAULT NULL,
    `created_evaluations_id` bigint        DEFAULT NULL,
    `goal_id`                bigint        DEFAULT NULL,
    `updated_evaluations_id` bigint        DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                      `FK_EVALUATIONS_CREATE_EMPLOYEES` (`created_evaluations_id`),
    KEY                      `FK_EVALUATIONS_GOALS` (`goal_id`),
    KEY                      `FK_EVALUATIONS_UPDATE_EMPLOYEES` (`updated_evaluations_id`),
    CONSTRAINT `FK_EVALUATIONS_CREATE_EMPLOYEES` FOREIGN KEY (`created_evaluations_id`) REFERENCES `employees` (`id`),
    CONSTRAINT `FK_EVALUATIONS_GOALS` FOREIGN KEY (`goal_id`) REFERENCES `goals` (`id`),
    CONSTRAINT `FK_EVALUATIONS_UPDATE_EMPLOYEES` FOREIGN KEY (`updated_evaluations_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `services`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `content`            varchar(1024) DEFAULT NULL,
    `end`                datetime(6) DEFAULT NULL,
    `minutes`            int    NOT NULL,
    `start`              datetime(6) DEFAULT NULL,
    `title`              varchar(64)   DEFAULT NULL,
    `unfinished`         bit(1) NOT NULL,
    `assistance_plan_id` bigint        DEFAULT NULL,
    `client_id`          bigint        DEFAULT NULL,
    `employee_id`        bigint        DEFAULT NULL,
    `hour_type_id`       bigint        DEFAULT NULL,
    `institution_id`     bigint        DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                  `FK_SERVICES_ASSISTANCE_PLANS` (`assistance_plan_id`),
    KEY                  `FK_SERVICES_CLIENTS` (`client_id`),
    KEY                  `FK_SERVICES_EMPLOYEES` (`employee_id`),
    KEY                  `FK_SERVICES_HOUR_TYPES` (`hour_type_id`),
    KEY                  `FK_SERVICES_INSTITUTIONS` (`institution_id`),
    CONSTRAINT `FK_SERVICES_EMPLOYEES` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
    CONSTRAINT `FK_SERVICES_HOUR_TYPES` FOREIGN KEY (`hour_type_id`) REFERENCES `hour_types` (`id`),
    CONSTRAINT `FK_SERVICES_INSTITUTIONS` FOREIGN KEY (`institution_id`) REFERENCES `institutions` (`id`),
    CONSTRAINT `FK_SERVICES_CLIENTS` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
    CONSTRAINT `FK_SERVICES_ASSISTANCE_PLANS` FOREIGN KEY (`assistance_plan_id`) REFERENCES `assistance_plans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `service_categories`
(
    `service_id`  bigint NOT NULL,
    `category_id` bigint NOT NULL,
    PRIMARY KEY (`service_id`, `category_id`),
    KEY           `FK_SERVICES_CATEGORIES_CATEGORIES` (`category_id`),
    CONSTRAINT `FK_SERVICES_CATEGORIES_CATEGORIES` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    CONSTRAINT `FK_SERVICES_CATEGORIES_SERVICES` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `service_goals`
(
    `service_id` bigint NOT NULL,
    `goal_id`    bigint NOT NULL,
    PRIMARY KEY (`service_id`, `goal_id`),
    KEY          `FK_SERVICES_GOALS_GOALS` (`goal_id`),
    CONSTRAINT `FK_SERVICES_GOALS_SERVICES` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`),
    CONSTRAINT `FK_SERVICES_GOALS_GOALS` FOREIGN KEY (`goal_id`) REFERENCES `goals` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;