CREATE TABLE IF NOT EXISTS gtp_user
(
    user_id          INT          NOT NULL AUTO_INCREMENT,
    login_name       VARCHAR(255) NOT NULL,
    password_value   VARCHAR(255) NOT NULL,
    first_name       VARCHAR(255) NULL,
    last_name        VARCHAR(255) NULL,
    email            VARCHAR(255) NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_gtp_user_login_name (login_name)
);
