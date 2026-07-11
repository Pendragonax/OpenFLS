CREATE TABLE `hour_corridors`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `title`                varchar(64) NOT NULL,
    `weekly_minutes_from`  int NOT NULL,
    `weekly_minutes_till`  int NOT NULL,
    `hour_type_id`         bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_HOUR_CORRIDORS_HOUR_TYPES` (`hour_type_id`),
    CONSTRAINT `FK_HOUR_CORRIDORS_HOUR_TYPES` FOREIGN KEY (`hour_type_id`) REFERENCES `hour_types` (`id`),
    CONSTRAINT `CHK_HOUR_CORRIDORS_RANGE` CHECK (`weekly_minutes_till` >= `weekly_minutes_from`),
    CONSTRAINT `CHK_HOUR_CORRIDORS_FROM_NON_NEGATIVE` CHECK (`weekly_minutes_from` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `assistance_plans`
    ADD COLUMN `hour_corridor_id` bigint DEFAULT NULL AFTER `sponsor_id`,
    ADD KEY `FK_ASSISTANCE_PLANS_HOUR_CORRIDORS` (`hour_corridor_id`),
    ADD CONSTRAINT `FK_ASSISTANCE_PLANS_HOUR_CORRIDORS` FOREIGN KEY (`hour_corridor_id`) REFERENCES `hour_corridors` (`id`);
