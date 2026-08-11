-- Additional series metadata fields sourced from providers that Komga
-- previously had nowhere to store:
--   SCORE                 provider rating (Komf otherwise smuggles it in as a "score:" tag)
--   SERIALIZATION         magazine the series runs in (Bangumi 连载杂志)
--   RELEASE_DATE          start of serialization; Komga only had dates at book level
--   ALTERNATE_PUBLISHERS  original vs localized publishers, mirroring the genre/tag pattern

alter table SERIES_METADATA
    add column SCORE real NULL;
alter table SERIES_METADATA
    add column SCORE_LOCK boolean NOT NULL DEFAULT 0;

alter table SERIES_METADATA
    add column SERIALIZATION varchar NOT NULL DEFAULT '';
alter table SERIES_METADATA
    add column SERIALIZATION_LOCK boolean NOT NULL DEFAULT 0;

alter table SERIES_METADATA
    add column RELEASE_DATE date NULL;
alter table SERIES_METADATA
    add column RELEASE_DATE_LOCK boolean NOT NULL DEFAULT 0;

CREATE TABLE SERIES_METADATA_ALTERNATE_PUBLISHER
(
    PUBLISHER varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

alter table SERIES_METADATA
    add column ALTERNATE_PUBLISHERS_LOCK boolean NOT NULL DEFAULT 0;

create index idx__series_metadata_alternate_publisher__series_id
    on SERIES_METADATA_ALTERNATE_PUBLISHER (SERIES_ID);
