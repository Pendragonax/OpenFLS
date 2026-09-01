CREATE TABLE `client_favorites`
(
    `employee_id` bigint NOT NULL,
    `client_id`   bigint NOT NULL,
    PRIMARY KEY (`employee_id`, `client_id`),
    KEY           `FK_CLIENT_FAVORITES_CLIENT` (`client_id`),
    CONSTRAINT `FK_CLIENT_FAVORITES_CLIENT` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
    CONSTRAINT `FK_CLIENT_FAVORITES_EMPLOYEES` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `client_tasks`
(
    `id`                    bigint       NOT NULL AUTO_INCREMENT,
    `client_id`             bigint       NOT NULL,
    `title`                 varchar(128) NOT NULL,
    `description`           varchar(1024)         DEFAULT NULL,
    `due_date`              date         NOT NULL,
    `created_by_id`         bigint                DEFAULT NULL,
    `created_at`            datetime     NOT NULL,
    `done`                  bit(1)       NOT NULL DEFAULT b'0',
    `completed_by_id`       bigint                DEFAULT NULL,
    `completed_at`          datetime              DEFAULT NULL,
    `completed_on`          date                  DEFAULT NULL,
    `completion_comment`    varchar(1024)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_CLIENT_TASKS_CLIENT` (`client_id`),
    KEY `FK_CLIENT_TASKS_CREATED_BY` (`created_by_id`),
    KEY `FK_CLIENT_TASKS_COMPLETED_BY` (`completed_by_id`),
    KEY `IDX_CLIENT_TASKS_CLIENT_DONE_DUE` (`client_id`, `done`, `due_date`),
    CONSTRAINT `FK_CLIENT_TASKS_CLIENT` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
    CONSTRAINT `FK_CLIENT_TASKS_CREATED_BY` FOREIGN KEY (`created_by_id`) REFERENCES `employees` (`id`),
    CONSTRAINT `FK_CLIENT_TASKS_COMPLETED_BY` FOREIGN KEY (`completed_by_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `client_task_audit_logs`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT,
    `client_task_id`    bigint       NOT NULL,
    `client_id`         bigint       NOT NULL,
    `action`            varchar(16)  NOT NULL,
    `changed_at`        datetime     NOT NULL,
    `actor_employee_id` bigint                DEFAULT NULL,
    `actor`             varchar(128) NOT NULL,
    `before_title`      varchar(128)          DEFAULT NULL,
    `after_title`       varchar(128)          DEFAULT NULL,
    `before_due_date`   date                  DEFAULT NULL,
    `after_due_date`    date                  DEFAULT NULL,
    `before_done`       bit(1)                DEFAULT NULL,
    `after_done`        bit(1)                DEFAULT NULL,
    `comment`           varchar(1024)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `IDX_CLIENT_TASK_AUDIT_LOGS_TASK` (`client_task_id`),
    KEY `IDX_CLIENT_TASK_AUDIT_LOGS_CLIENT` (`client_id`),
    KEY `IDX_CLIENT_TASK_AUDIT_LOGS_CHANGED_AT` (`changed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
