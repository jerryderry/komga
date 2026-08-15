import type { SeriesMetadataDto, SeriesMetadataUpdateDto } from '@/generated/openapi'

export function seriesMetadataToDto(metadata: SeriesMetadataDto): SeriesMetadataUpdateDto {
  return Object.assign({}, metadata, {
    // The read DTO spells "unset" as an empty string for these two, but an empty
    // string is not a member of either enum. The update DTO unsets with null.
    readingDirection: (metadata.readingDirection || null) as
      | 'LEFT_TO_RIGHT'
      | 'RIGHT_TO_LEFT'
      | 'VERTICAL'
      | 'WEBTOON',
    bookUnit: (metadata.bookUnit || null) as 'VOLUME' | 'CHAPTER' | 'ISSUE',
    status: metadata.status as 'ENDED' | 'ONGOING' | 'ABANDONED' | 'HIATUS',
  })
}
