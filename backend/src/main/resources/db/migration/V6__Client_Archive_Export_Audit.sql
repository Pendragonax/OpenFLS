ALTER TABLE `client_archive_history_entries`
    ADD COLUMN `export_format` varchar(32) DEFAULT NULL AFTER `action_type`;
