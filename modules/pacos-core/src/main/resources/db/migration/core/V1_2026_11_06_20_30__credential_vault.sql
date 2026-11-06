CREATE TABLE PACOS_CREDENTIAL
(
    id              VARCHAR(36) PRIMARY KEY NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(2000),
    credential_type VARCHAR(128) NOT NULL,
    owner_user_id   INTEGER NOT NULL REFERENCES APP_USER (id) ON DELETE CASCADE,
    visibility      VARCHAR(16) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE INDEX IDX_CREDENTIAL_OWNER ON PACOS_CREDENTIAL (owner_user_id);
CREATE INDEX IDX_CREDENTIAL_VISIBILITY ON PACOS_CREDENTIAL (visibility);

CREATE TABLE PACOS_CREDENTIAL_VALUE
(
    id              VARCHAR(36) PRIMARY KEY NOT NULL,
    credential_id   VARCHAR(36) NOT NULL REFERENCES PACOS_CREDENTIAL (id) ON DELETE CASCADE,
    field_key       VARCHAR(128) NOT NULL,
    field_type      VARCHAR(32) NOT NULL,
    encrypted_value BLOB NOT NULL,
    CONSTRAINT UK_CREDENTIAL_VALUE_FIELD UNIQUE (credential_id, field_key)
);

CREATE TABLE PACOS_CREDENTIAL_FILE
(
    id                VARCHAR(36) PRIMARY KEY NOT NULL,
    credential_id     VARCHAR(36) NOT NULL REFERENCES PACOS_CREDENTIAL (id) ON DELETE CASCADE,
    field_key         VARCHAR(128) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(255),
    file_size         BIGINT NOT NULL,
    checksum          VARCHAR(64) NOT NULL,
    storage_reference VARCHAR(36) NOT NULL UNIQUE,
    CONSTRAINT UK_CREDENTIAL_FILE_FIELD UNIQUE (credential_id, field_key)
);

CREATE TABLE PACOS_CREDENTIAL_ACCESS
(
    id             VARCHAR(36) PRIMARY KEY NOT NULL,
    credential_id  VARCHAR(36) NOT NULL REFERENCES PACOS_CREDENTIAL (id) ON DELETE CASCADE,
    principal_type VARCHAR(16) NOT NULL,
    principal_id   VARCHAR(255) NOT NULL,
    permission     VARCHAR(16) NOT NULL,
    CONSTRAINT UK_CREDENTIAL_ACCESS UNIQUE (credential_id, principal_type, principal_id, permission)
);

CREATE INDEX IDX_CREDENTIAL_ACCESS_PRINCIPAL
    ON PACOS_CREDENTIAL_ACCESS (principal_type, principal_id);
