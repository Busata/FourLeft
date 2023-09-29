drop table fiaticker_entry;

CREATE TABLE fiaticker_entry
(
    id                     UUID NOT NULL,
    event_id               VARCHAR,
    time                   TIMESTAMP WITHOUT TIME ZONE,
    reference_id           VARCHAR,
    text_html              VARCHAR,
    text_markdown          VARCHAR,
    title                  VARCHAR,
    ticker_entry_image_url VARCHAR,
    ticker_event_key       VARCHAR,
    source                 VARCHAR,
    CONSTRAINT pk_fiatickerentry PRIMARY KEY (id)
);