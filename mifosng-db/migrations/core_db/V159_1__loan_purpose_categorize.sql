ALTER TABLE `m_code` ADD COLUMN `category_id` INT(10) NULL AFTER `is_system_defined`;

INSERT INTO `m_code` (`code_name`, `is_system_defined`, `category_id`) VALUES ('CodeCategories', 1, NULL);
 
ALTER TABLE `m_product_loan` ADD COLUMN `purpose_category_code_id` INT NULL DEFAULT NULL AFTER `close_date`;