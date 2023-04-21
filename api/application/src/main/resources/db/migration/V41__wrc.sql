CREATE TABLE wrcticker_entry
(
    id                     UUID NOT NULL,
    event_id               VARCHAR,
    time                   TIMESTAMP with time zone,
    reference_id           VARCHAR,
    text_html                  VARCHAR,
    text_markdown                   VARCHAR,
    title                  VARCHAR,
    ticker_entry_image_url VARCHAR,
    ticker_event_key       VARCHAR,
    CONSTRAINT pk_wrctickerentry PRIMARY KEY (id)
);