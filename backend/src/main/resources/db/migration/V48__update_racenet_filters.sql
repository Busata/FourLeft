ALTER TABLE racenet_filter
    ADD enabled BOOLEAN;

update racenet_filter set enabled = true;

ALTER TABLE racenet_filter
    ALTER COLUMN enabled SET NOT NULL;

