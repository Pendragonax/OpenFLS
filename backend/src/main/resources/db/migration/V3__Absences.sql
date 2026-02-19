CREATE TABLE `absences`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `absence_date` date   NOT NULL,
    `employee_id`  bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UQ_ABSENCES_EMPLOYEE_DATE` (`employee_id`, `absence_date`),
    KEY            `FK_ABSENCES_EMPLOYEES` (`employee_id`),
    CONSTRAINT `FK_ABSENCES_EMPLOYEES` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
