CREATE TABLE `hour_corridor_audit_logs` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `hour_corridor_id` bigint NOT NULL,
    `action` varchar(16) NOT NULL,
    `changed_at` datetime NOT NULL,
    `actor` varchar(128) NOT NULL,
    `before_title` varchar(255),
    `after_title` varchar(255),
    `before_weekly_minutes_from` int,
    `after_weekly_minutes_from` int,
    `before_weekly_minutes_till` int,
    `after_weekly_minutes_till` int,
    `before_hour_type_id` bigint,
    `after_hour_type_id` bigint,
    PRIMARY KEY (`id`),
    INDEX `idx_hour_corridor_audit_logs_corridor` (`hour_corridor_id`),
    INDEX `idx_hour_corridor_audit_logs_changed_at` (`changed_at`)
);
