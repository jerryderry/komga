package org.gotson.komga.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.SeriesMetadata.BookUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookUnitAnalyzerTest {
  private val analyzer = BookUnitAnalyzer()

  @Nested
  inner class SingleName {
    @Test
    fun `given chapter markers when reading a name then it is a chapter`() {
      listOf(
        "Some Series ch53",
        "Some Series_ch53",
        "Some Series - Chapter 53",
        "Some Series ch. 53",
        "Some Series Ep.53",
        "[Circle] Some Series c53",
        "とある作品 第53話",
        "某作品 第53话",
      ).forEach {
        assertThat(analyzer.unitOf(it)).describedAs(it).isEqualTo(BookUnit.CHAPTER)
        assertThat(analyzer.numberOf(it, BookUnit.CHAPTER)).describedAs(it).isEqualTo(53f)
      }
    }

    @Test
    fun `given volume markers when reading a name then it is a volume`() {
      listOf(
        "Some Series v03",
        "Some Series Vol. 3",
        "Some Series - Volume 3",
        "とある作品 第3巻",
      ).forEach {
        assertThat(analyzer.unitOf(it)).describedAs(it).isEqualTo(BookUnit.VOLUME)
        assertThat(analyzer.numberOf(it, BookUnit.VOLUME)).describedAs(it).isEqualTo(3f)
      }
    }

    @Test
    fun `given both markers when reading a name then volume wins`() {
      val name = "Some Series v03 c015-c022"
      assertThat(analyzer.unitOf(name)).isEqualTo(BookUnit.VOLUME)
      assertThat(analyzer.numberOf(name, BookUnit.VOLUME)).isEqualTo(3f)
    }

    @Test
    fun `given a convention code when reading a name then it is not a chapter`() {
      // (C105) is Comiket 105. Reading it as chapter 105 would mislabel most of a
      // doujinshi library, which is why the bare "c" form requires a leading space.
      listOf(
        "(C105) [Circle (Artist)] Some Title",
        "(COMIC1 15) [Circle] Some Title",
      ).forEach {
        assertThat(analyzer.unitOf(it)).describedAs(it).isNull()
      }
    }

    @Test
    fun `given no marker when reading a name then there is no unit`() {
      listOf(
        "Some Standalone Title",
        "[Circle (Artist)] Some Title (Parody) [DL版]",
      ).forEach {
        assertThat(analyzer.unitOf(it)).describedAs(it).isNull()
      }
    }

    @Test
    fun `given a decimal chapter when reading a name then the fraction is kept`() {
      assertThat(analyzer.numberOf("Some Series ch53.5", BookUnit.CHAPTER)).isEqualTo(53.5f)
      assertThat(analyzer.numberOf("Some Series ch53x5", BookUnit.CHAPTER)).isEqualTo(53.5f)
    }
  }

  @Nested
  inner class SeriesDetection {
    @Test
    fun `given a majority of chapters when detecting then the series is chapters`() {
      val names = (1..10).map { "Some Series ch%02d".format(it) } + listOf("Some Series extra")
      assertThat(analyzer.detectUnit(names)).isEqualTo(BookUnit.CHAPTER)
    }

    @Test
    fun `given a minority of chapters when detecting then there is no unit`() {
      val names = listOf("Some Series ch01") + (1..10).map { "Standalone Title $it of many words" }
      assertThat(analyzer.detectUnit(names)).isNull()
    }

    @Test
    fun `given no books when detecting then there is no unit`() {
      assertThat(analyzer.detectUnit(emptyList())).isNull()
    }
  }

  @Nested
  inner class SeriesNumbering {
    @Test
    fun `given every book numbered when reading numbers then all are returned`() {
      val names = listOf("Some Series ch51", "Some Series ch52", "Some Series ch53")
      assertThat(analyzer.numbersFor(names, BookUnit.CHAPTER)).containsExactly(51f, 52f, 53f)
    }

    @Test
    fun `given one book unnumbered when reading numbers then none are returned`() {
      val names = listOf("Some Series ch51", "Some Series omake", "Some Series ch53")
      assertThat(analyzer.numbersFor(names, BookUnit.CHAPTER)).isNull()
    }
  }

  @Test
  fun `given a number when formatting then trailing zeroes are dropped`() {
    assertThat(analyzer.format(53f)).isEqualTo("53")
    assertThat(analyzer.format(53.5f)).isEqualTo("53.5")
  }
}
