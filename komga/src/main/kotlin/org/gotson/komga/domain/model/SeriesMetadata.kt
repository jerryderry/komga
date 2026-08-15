package org.gotson.komga.domain.model

import org.gotson.komga.language.lowerNotBlank
import java.time.LocalDate
import java.time.LocalDateTime

class SeriesMetadata(
  val status: Status = Status.ONGOING,
  title: String,
  titleSort: String = title,
  summary: String = "",
  val readingDirection: ReadingDirection? = null,
  val bookUnit: BookUnit? = null,
  publisher: String = "",
  alternatePublishers: Set<String> = emptySet(),
  serialization: String = "",
  val ageRating: Int? = null,
  val score: Float? = null,
  val releaseDate: LocalDate? = null,
  language: String = "",
  genres: Set<String> = emptySet(),
  tags: Set<String> = emptySet(),
  val totalBookCount: Int? = null,
  sharingLabels: Set<String> = emptySet(),
  val links: List<WebLink> = emptyList(),
  val alternateTitles: List<AlternateTitle> = emptyList(),
  val statusLock: Boolean = false,
  val titleLock: Boolean = false,
  val titleSortLock: Boolean = false,
  val summaryLock: Boolean = false,
  val readingDirectionLock: Boolean = false,
  val bookUnitLock: Boolean = false,
  val publisherLock: Boolean = false,
  val alternatePublishersLock: Boolean = false,
  val serializationLock: Boolean = false,
  val ageRatingLock: Boolean = false,
  val scoreLock: Boolean = false,
  val releaseDateLock: Boolean = false,
  val languageLock: Boolean = false,
  val genresLock: Boolean = false,
  val tagsLock: Boolean = false,
  val totalBookCountLock: Boolean = false,
  val sharingLabelsLock: Boolean = false,
  val linksLock: Boolean = false,
  val alternateTitlesLock: Boolean = false,
  val seriesId: String = "",
  override val createdDate: LocalDateTime = LocalDateTime.now(),
  override val lastModifiedDate: LocalDateTime = createdDate,
) : Auditable {
  val title = title.trim()
  val titleSort = titleSort.trim()
  val summary = summary.trim()
  val publisher = publisher.trim()
  val serialization = serialization.trim()
  val language = BCP47TagValidator.normalize(language.trim())
  val tags = tags.lowerNotBlank().toSet()
  val genres = genres.lowerNotBlank().toSet()
  val sharingLabels = sharingLabels.lowerNotBlank().toSet()

  // Publisher names are proper nouns, so unlike genres/tags/sharingLabels they keep
  // their casing and are only trimmed and de-blanked.
  val alternatePublishers = alternatePublishers.mapNotNull { it.trim().ifBlank { null } }.toSet()

  fun copy(
    status: Status = this.status,
    title: String = this.title,
    titleSort: String = this.titleSort,
    summary: String = this.summary,
    readingDirection: ReadingDirection? = this.readingDirection,
    bookUnit: BookUnit? = this.bookUnit,
    publisher: String = this.publisher,
    alternatePublishers: Set<String> = this.alternatePublishers,
    serialization: String = this.serialization,
    ageRating: Int? = this.ageRating,
    score: Float? = this.score,
    releaseDate: LocalDate? = this.releaseDate,
    language: String = this.language,
    genres: Set<String> = this.genres,
    tags: Set<String> = this.tags,
    totalBookCount: Int? = this.totalBookCount,
    sharingLabels: Set<String> = this.sharingLabels,
    links: List<WebLink> = this.links,
    alternateTitles: List<AlternateTitle> = this.alternateTitles,
    statusLock: Boolean = this.statusLock,
    titleLock: Boolean = this.titleLock,
    titleSortLock: Boolean = this.titleSortLock,
    summaryLock: Boolean = this.summaryLock,
    readingDirectionLock: Boolean = this.readingDirectionLock,
    bookUnitLock: Boolean = this.bookUnitLock,
    publisherLock: Boolean = this.publisherLock,
    alternatePublishersLock: Boolean = this.alternatePublishersLock,
    serializationLock: Boolean = this.serializationLock,
    ageRatingLock: Boolean = this.ageRatingLock,
    scoreLock: Boolean = this.scoreLock,
    releaseDateLock: Boolean = this.releaseDateLock,
    languageLock: Boolean = this.languageLock,
    genresLock: Boolean = this.genresLock,
    tagsLock: Boolean = this.tagsLock,
    totalBookCountLock: Boolean = this.totalBookCountLock,
    sharingLabelsLock: Boolean = this.sharingLabelsLock,
    linksLock: Boolean = this.linksLock,
    alternateTitlesLock: Boolean = this.alternateTitlesLock,
    seriesId: String = this.seriesId,
    createdDate: LocalDateTime = this.createdDate,
    lastModifiedDate: LocalDateTime = this.lastModifiedDate,
  ) = SeriesMetadata(
    status = status,
    title = title,
    titleSort = titleSort,
    summary = summary,
    readingDirection = readingDirection,
    bookUnit = bookUnit,
    publisher = publisher,
    alternatePublishers = alternatePublishers,
    serialization = serialization,
    ageRating = ageRating,
    score = score,
    releaseDate = releaseDate,
    language = language,
    genres = genres,
    tags = tags,
    totalBookCount = totalBookCount,
    sharingLabels = sharingLabels,
    links = links,
    alternateTitles = alternateTitles,
    statusLock = statusLock,
    titleLock = titleLock,
    titleSortLock = titleSortLock,
    summaryLock = summaryLock,
    readingDirectionLock = readingDirectionLock,
    bookUnitLock = bookUnitLock,
    publisherLock = publisherLock,
    alternatePublishersLock = alternatePublishersLock,
    serializationLock = serializationLock,
    ageRatingLock = ageRatingLock,
    scoreLock = scoreLock,
    releaseDateLock = releaseDateLock,
    languageLock = languageLock,
    genresLock = genresLock,
    tagsLock = tagsLock,
    totalBookCountLock = totalBookCountLock,
    sharingLabelsLock = sharingLabelsLock,
    linksLock = linksLock,
    alternateTitlesLock = alternateTitlesLock,
    seriesId = seriesId,
    createdDate = createdDate,
    lastModifiedDate = lastModifiedDate,
  )

  enum class Status {
    ENDED,
    ONGOING,
    ABANDONED,
    HIATUS,
  }

  enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON,
  }

  /**
   * What one book of the series represents. Orthogonal to [ReadingDirection], which
   * describes how pages are laid out: a right-to-left manga can be released by
   * chapter, and a vertical-scrolling series can be collected into volumes.
   *
   * Null means unknown, which is the honest answer for a series whose filenames say
   * nothing - a collection of one-shots, or a folder named by title alone.
   */
  enum class BookUnit {
    VOLUME,
    CHAPTER,
    ISSUE,
  }

  override fun toString(): String =
    "SeriesMetadata(status=$status, readingDirection=$readingDirection, bookUnit=$bookUnit, ageRating=$ageRating, score=$score, releaseDate=$releaseDate, totalBookCount=$totalBookCount, links=$links, alternateTitles=$alternateTitles, statusLock=$statusLock, titleLock=$titleLock, titleSortLock=$titleSortLock, summaryLock=$summaryLock, readingDirectionLock=$readingDirectionLock, bookUnitLock=$bookUnitLock, publisherLock=$publisherLock, alternatePublishersLock=$alternatePublishersLock, serializationLock=$serializationLock, ageRatingLock=$ageRatingLock, scoreLock=$scoreLock, releaseDateLock=$releaseDateLock, languageLock=$languageLock, genresLock=$genresLock, tagsLock=$tagsLock, totalBookCountLock=$totalBookCountLock, sharingLabelsLock=$sharingLabelsLock, linksLock=$linksLock, alternateTitlesLock=$alternateTitlesLock, seriesId='$seriesId', createdDate=$createdDate, lastModifiedDate=$lastModifiedDate, title='$title', titleSort='$titleSort', summary='$summary', publisher='$publisher', alternatePublishers=$alternatePublishers, serialization='$serialization', language='$language', tags=$tags, genres=$genres, sharingLabels=$sharingLabels)"
}
