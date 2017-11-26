CREATE DATABASE logon;
\c logon
CREATE TABLE IF NOT EXISTS accounts (
    name            varchar(16) PRIMARY KEY,
    mail            varchar(64) NOT NULL,
    pwd_hash        char(40),
    pwd_salt        char(64),
    session_key     char(64)
);

/*

LATER?

last_ip
gmlevel
joindate
last_login
locked / banned
version

https://trinitycore.atlassian.net/wiki/display/tc/account

*/
