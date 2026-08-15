package org.gotson.komga.domain.service

import org.gotson.komga.domain.model.SeriesMetadata.BookUnit
import org.springframework.stereotype.Service

/**
 * Works out whether a series is divided into volumes, chapters or issues by reading
 * the markers in its book filenames, and extracts the number each filename claims.
 *
 * Komga otherwise numbers books by their position in the sorted list, which is right
 * when the filenames carry no number and wrong the moment they do: a folder of
 * chapters 51-80 becomes books 1-30, and adding chapter 50 later renumbers all of
 * them.
 */
@Service
class BookUnitAnalyzer {
  /**
   * A file can legitimately carry both markers - "v03 c015-c022" is volume three,
   * which happens to contain those chapters - so volume wins where both appear.
   * Issue is last because its patterns are the loosest.
   */
  private val patternsByUnit: List<Pair<BookUnit, List<Regex>>> =
    listOf(
      BookUnit.VOLUME to
        listOf(
          """(?i)(?:^|[\s_\-\[(])(?:v|vol|vols|volume)\.?[\s_\-]*(?<number>[0-9]+([.x#][0-9]+)?)""".toRegex(),
          """第(?<number>\d+(\.\d+)?)-?\d*(\.\d+)?\s*巻""".toRegex(),
        ),
      BookUnit.CHAPTER to
        listOf(
          // Chapter markers appear with any separator, not just a space: "ch. 12",
          // "ch12", "_ch12", "-Ep.12". The bare "c" form stays restricted to a
          // preceding space, because convention codes look identical otherwise -
          // "(C105)" is Comiket 105, not chapter 105, and a looser pattern misreads
          // a large part of a doujinshi library as chapters.
          (
            """(?i)(?:(?<=\s)c|(?:^|[\s_\-\[(])(?:chapter|episode|ch|ep))""" +
              """\.?[\s_\-]*(?<number>[0-9]+([.x#][0-9]+)?)"""
          ).toRegex(),
          // 話 is the Japanese form, 话 the simplified Chinese one used by
          // Chinese-language releases.
          """第(?<number>\d+(\.\d+)?)-?\d*(\.\d+)?\s*[話话]""".toRegex(),
        ),
      BookUnit.ISSUE to
        listOf(
          """(?i)(?:^|[\s_\-\[(])(?:issue|#)\.?[\s_\-]*(?<number>[0-9]+([.x#][0-9]+)?)""".toRegex(),
        ),
    )

  /**
   * The unit [name] carries a marker for, or null if it carries none. Where a name
   * matches more than one pattern the last match wins, so a title that happens to
   * contain "Vol" loses to the marker at the end of the filename.
   */
  fun unitOf(name: String): BookUnit? = patternsByUnit.firstOrNull { (_, patterns) -> numberIn(name, patterns) != null }?.first

  fun numberOf(
    name: String,
    unit: BookUnit,
  ): Float? = patternsByUnit.firstOrNull { it.first == unit }?.let { numberIn(name, it.second) }

  private fun numberIn(
    name: String,
    patterns: List<Regex>,
  ): Float? =
    patterns
      .firstNotNullOfOrNull { it.findAll(name).lastOrNull()?.groups?.get("number")?.value }
      ?.replace("[x#]".toRegex(), ".")
      ?.toFloatOrNull()

  /**
   * The unit the majority of [names] agree on, or null when they do not.
   *
   * A strict majority is required rather than any match at all: one stray "ch01" in a
   * folder of thirty untitled one-shots says nothing about the folder, and labelling
   * the whole series from it would be worse than admitting ignorance.
   */
  fun detectUnit(names: Collection<String>): BookUnit? {
    if (names.isEmpty()) return null
    val counts = names.mapNotNull { unitOf(it) }.groupingBy { it }.eachCount()
    val (unit, count) = counts.maxByOrNull { it.value } ?: return null
    return if (count * 2 > names.size) unit else null
  }

  /**
   * The number each name claims for [unit], but only when every one of them claims
   * one. A partial answer is not useful here: mixing parsed numbers with positional
   * ones would order a series by two incompatible schemes at once.
   */
  fun numbersFor(
    names: List<String>,
    unit: BookUnit,
  ): List<Float>? {
    val numbers = names.map { numberOf(it, unit) }
    return if (numbers.all { it != null }) numbers.filterNotNull() else null
  }

  /** Renders a parsed number the way a filename would write it: 53, not 53.0. */
  fun format(number: Float): String = if (number == number.toInt().toFloat()) number.toInt().toString() else number.toString()
}
