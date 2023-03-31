CREATE TABLE wrcevent
(
    id               UUID    NOT NULL,
    active           BOOLEAN NOT NULL,
    name             VARCHAR,
    wrc_reference_id VARCHAR,
    CONSTRAINT pk_wrcevent PRIMARY KEY (id)
);

CREATE TABLE wrcticker_entry
(
    id                  UUID NOT NULL,
    event_id            VARCHAR,
    ticker_reference_id VARCHAR,
    date_time           TIMESTAMP WITHOUT TIME ZONE,
    title               VARCHAR,
    text                VARCHAR,
    image_url           VARCHAR,
    CONSTRAINT pk_wrctickerentry PRIMARY KEY (id)
);