CREATE TABLE fiaticker_entry
(
    id                     UUID NOT NULL,
    event_id               VARCHAR(255),
    time                   TIMESTAMP WITHOUT TIME ZONE,
    reference_id           VARCHAR(255),
    text_html              VARCHAR(255),
    text_markdown          VARCHAR(255),
    title                  VARCHAR(255),
    ticker_entry_image_url VARCHAR(255),
    ticker_event_key       VARCHAR(255),
    source                 VARCHAR(255),
    CONSTRAINT pk_fiatickerentry PRIMARY KEY (id)
);