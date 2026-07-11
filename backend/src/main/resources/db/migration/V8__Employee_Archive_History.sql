ALTER TABLE `employees`
    ADD COLUMN `archived` bit(1) NOT NULL DEFAULT 0 AFTER `email`;

CREATE TABLE `employee_archive_history_entries`
(
    `id`                           bigint NOT NULL AUTO_INCREMENT,
    `action_date`                  date          NOT NULL,
    `action_timestamp`             datetime(6)   NOT NULL,
    `action_type`                  varchar(32)   NOT NULL,
    `reason`                       varchar(1024) NOT NULL,
    `remark`                       varchar(1024) NOT NULL,
    `executing_employee_id`        bigint        NOT NULL,
    `executing_employee_firstname` varchar(64)   NOT NULL,
    `executing_employee_lastname`  varchar(64)   NOT NULL,
    `employee_id`                  bigint        DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_EMPLOYEE_ARCHIVE_HISTORY_ENTRIES_EMPLOYEE` (`employee_id`),
    KEY `IDX_EMPLOYEE_ARCHIVE_HISTORY_ENTRIES_ACTION_TIMESTAMP` (`action_timestamp`),
    CONSTRAINT `FK_EMPLOYEE_ARCHIVE_HISTORY_ENTRIES_EMPLOYEE` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
