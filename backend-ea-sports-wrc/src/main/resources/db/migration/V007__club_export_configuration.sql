CREATE SEQUENCE IF NOT EXISTS club_export_configuration_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE club_export_configuration
(
    id      BIGINT  NOT NULL,
    club_id VARCHAR(255),
    enabled BOOLEAN NOT NULL,
    CONSTRAINT pk_clubexportconfiguration PRIMARY KEY (id)
);
