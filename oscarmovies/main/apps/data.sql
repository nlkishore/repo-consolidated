# cat data.sql

DROP SCHEMA IF EXISTS `oscarmoviesdb` ;
CREATE SCHEMA IF NOT EXISTS `oscarmoviesdb`;


CREATE TABLE IF NOT EXISTS `oscarmoviesdb`.`genres` (
  `gen_id` VARCHAR(5) NOT NULL ,
  `gen_title` VARCHAR(45) NOT NULL,
  `gen_desc` VARCHAR(2000) NULL DEFAULT NULL,
  PRIMARY KEY (`gen_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `oscarmoviesdb`.`movie` (
  `mov_id` VARCHAR(5) NOT NULL,
  `mov_title` VARCHAR(50) NOT NULL,
  `mov_year` INT NOT NULL,
  `mov_time` INT NULL DEFAULT NULL,
  `mov_lang` VARCHAR(45) NULL DEFAULT NULL,
  `mov_rel_country` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`mov_id`));

  CREATE TABLE IF NOT EXISTS `oscarmoviesdb`.`movie_genres` (
  `mov_id` VARCHAR(5) NOT NULL,
  `gen_id` VARCHAR(5) NOT NULL,
  INDEX `mov_gen_mov_id_idx` (`mov_id` ASC) VISIBLE,
  INDEX `mov_gen_gen_id_idx` (`gen_id` ASC) VISIBLE,
  CONSTRAINT `mov_gen_gen_id`
    FOREIGN KEY (`gen_id`)
    REFERENCES `oscarmoviesdb`.`genres` (`gen_id`),
  CONSTRAINT `mov_gen_mov_id`
    FOREIGN KEY (`mov_id`)
    REFERENCES `oscarmoviesdb`.`movie` (`mov_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;