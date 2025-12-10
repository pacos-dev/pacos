create table IF NOT EXISTS PUBLIC.APP_API_TOKEN
(
    NAME               VARCHAR(255) PRIMARY KEY not null,
    TOKEN              VARCHAR(255) not null,
    EXPIRED_ON         DATE,

    STATUS             VARCHAR(50)  not null,
    CREATION_TIMESTAMP TIMESTAMP    not null,
    UPDATE_TIMESTAMP   TIMESTAMP    not null,
    CREATED_BY         INTEGER      not null,
    UPDATED_BY         INTEGER      not null
);