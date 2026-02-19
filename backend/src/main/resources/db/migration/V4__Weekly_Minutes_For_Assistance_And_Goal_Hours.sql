ALTER TABLE `assistance_plan_hours`
    ADD COLUMN `weekly_minutes` INT NOT NULL DEFAULT 0 AFTER `weekly_hours`;

UPDATE `assistance_plan_hours`
SET `weekly_minutes` = ROUND(`weekly_hours` * 60);

ALTER TABLE `assistance_plan_hours`
    DROP COLUMN `weekly_hours`;

ALTER TABLE `goal_hours`
    ADD COLUMN `weekly_minutes` INT NOT NULL DEFAULT 0 AFTER `weekly_hours`;

UPDATE `goal_hours`
SET `weekly_minutes` = ROUND(`weekly_hours` * 60);

ALTER TABLE `goal_hours`
    DROP COLUMN `weekly_hours`;
