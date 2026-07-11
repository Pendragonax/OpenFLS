ALTER TABLE `assistance_plans`
    ADD COLUMN `hour_mode` varchar(16) NOT NULL DEFAULT 'EXACT' AFTER `end`;
