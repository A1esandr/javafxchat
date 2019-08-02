CREATE TABLE user (
    id     INTEGER       PRIMARY KEY AUTOINCREMENT
                         NOT NULL,
    login  VARCHAR (50)  NOT NULL,
    pass   VARCHAR (256) NOT NULL,
    nick   VARCHAR (50)  NOT NULL
);

-- Sample data
INSERT INTO user(login, pass, nick) VALUES ('login1', 'pass1', 'nick1'), ('login2', 'pass2', 'nick2'), ('login3', 'pass3', 'nick3');