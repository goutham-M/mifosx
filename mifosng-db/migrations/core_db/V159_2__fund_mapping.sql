INSERT INTO `m_code` (`code_name`, `is_system_defined`) VALUES ('FundType', 1);

ALTER TABLE `m_loan` 
ADD COLUMN `fund_type_cv_id` INT NULL DEFAULT NULL AFTER `fund_id`,
ADD COLUMN `funding_date` DATE NULL DEFAULT NULL,
ADD INDEX `FKCFCEA42640BE08319` (`fund_type_cv_id`),
ADD CONSTRAINT `FKCFCEA42640BE08319` FOREIGN KEY (`fund_type_cv_id`) REFERENCES `m_code_value` (`id`);

CREATE TABLE `m_fund_loan_mapping_history` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`loan_id` BIGINT(20) NOT NULL,
	`fund_id` BIGINT(20) NOT NULL,
	`fund_type_cv_id` INT(20) NULL,
	`start_date` DATE NULL DEFAULT NULL,
	`end_date` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_m_loan_fund_map` (`loan_id`),
	INDEX `FK_m_fundid_fund_map` (`fund_id`),
	INDEX `FK_m_code_value_fund_map` (`fund_type_cv_id`),
	CONSTRAINT `FK_m_loan_fund_map` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
	CONSTRAINT `FK_m_fundid_fund_map` FOREIGN KEY (`fund_id`) REFERENCES `m_fund` (`id`),
	CONSTRAINT `FK_m_code_value_fund_map` FOREIGN KEY (`fund_type_cv_id`) REFERENCES `m_code_value` (`id`)
);

ALTER TABLE `m_loan`
CHANGE COLUMN `funding_date` `funding_date` DATE NULL DEFAULT NULL AFTER `fund_id`;
