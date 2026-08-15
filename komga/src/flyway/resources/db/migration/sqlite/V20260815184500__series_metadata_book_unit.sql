-- What one book of a series represents: VOLUME, CHAPTER or ISSUE.
--
-- Komga had no way to express this. A series released by chapter and one
-- collected into volumes were both just "books", so the UI could only ever say
-- "120 books" and book numbering had nothing to anchor to. Reading direction was
-- the closest existing field and it is the wrong one - it describes page layout,
-- not how the series is divided.
--
-- NULL means unknown, which is correct for a folder of one-shots or any set of
-- filenames that carry no volume or chapter marker.

alter table SERIES_METADATA
    add column BOOK_UNIT varchar NULL;
alter table SERIES_METADATA
    add column BOOK_UNIT_LOCK boolean NOT NULL DEFAULT 0;
