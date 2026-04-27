-- -----------------------------------------------------------------------
-- mysql SQL script for schema uob_portal (matches Torque generator output for demo schema)
-- -----------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS gtp_user;

CREATE TABLE gtp_user
(
    user_id INTEGER NOT NULL AUTO_INCREMENT,
    login_name VARCHAR(255) NOT NULL,
    password_value VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    PRIMARY KEY (user_id),
    UNIQUE uk_gtp_user_login_name (login_name)
);
