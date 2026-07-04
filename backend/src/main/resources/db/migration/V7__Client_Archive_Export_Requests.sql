CREATE TABLE `client_archive_export_requests`
(
    `id`                              bigint NOT NULL AUTO_INCREMENT,
    `download_token`                  varchar(64)  NOT NULL,
    `export_format`                   varchar(32)  NOT NULL,
    `requested_at`                    datetime(6)  NOT NULL,
    `expires_at`                      datetime(6)  NOT NULL,
    `downloaded_at`                   datetime(6)  DEFAULT NULL,
    `file_name`                       varchar(255) NOT NULL,
    `file_path`                       varchar(1024) NOT NULL,
    `requested_by_employee_id`        bigint       NOT NULL,
    `requested_by_employee_firstname` varchar(64)  NOT NULL,
    `requested_by_employee_lastname`  varchar(64)  NOT NULL,
    `client_id`                       bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_CLIENT_ARCHIVE_EXPORT_REQUESTS_TOKEN` (`download_token`),
    KEY `IDX_CLIENT_ARCHIVE_EXPORT_REQUESTS_CLIENT_ID` (`client_id`),
    KEY `IDX_CLIENT_ARCHIVE_EXPORT_REQUESTS_EXPIRES_AT` (`expires_at`),
    CONSTRAINT `FK_CLIENT_ARCHIVE_EXPORT_REQUESTS_CLIENT` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
