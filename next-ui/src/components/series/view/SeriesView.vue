<template>
  <v-container fluid>
    <v-row>
      <v-col
        cols="6"
        sm="3"
      >
        <ItemPoster
          :poster-url="seriesPosterUrl(series.id)"
          :top-right-icon="isRead ? 'i-mdi:check' : undefined"
          :top-right="unreadCount"
          :max-width="posterMaxWidth"
        />

        <v-alert
          v-if="isRead || bookOnDeck"
          :icon="isRead ? 'i-mdi:check' : undefined"
          class="mt-1 text-center text-body-small"
          :max-width="posterMaxWidth"
        >
          <template v-if="bookOnDeck">{{
            $formatMessage(
              {
                description: 'Series view: book on deck',
                defaultMessage: 'On deck — #{number}',
                id: '5cbjLE',
              },
              { number: bookOnDeck.metadata.number },
            )
          }}</template>
          <template v-if="isRead">{{
            $formatMessage({
              description: 'Series view: read indicator',
              defaultMessage: 'Read',
              id: 'l7mpQK',
            })
          }}</template>
        </v-alert>
      </v-col>

      <v-col
        cols="6"
        sm="9"
      >
        <v-container class="pa-0">
          <v-row>
            <v-col>
              <div class="text-headline-small">{{ series.metadata.title }}</div>
            </v-col>
          </v-row>

          <v-row density="compact">
            <v-col>
              <SimpleDataTable :rows="alternateTitles" />
            </v-col>
          </v-row>

          <v-row
            density="comfortable"
            align="baseline"
          >
            <v-col cols="auto">
              <div class="text-body-medium">
                <span>{{ bookCountText }}</span>
              </div>
            </v-col>

            <v-col
              v-if="series.booksMetadata.releaseDate"
              cols="auto"
            >
              <div class="text-body-medium">
                {{
                  $formatDate(series.booksMetadata.releaseDate, {
                    year: 'numeric',
                    timeZone: 'UTC',
                  })
                }}
              </div>
            </v-col>
          </v-row>
        </v-container>

        <div :id="`sm-${id}`" />
      </v-col>
    </v-row>

    <div :id="`xs-${id}`" />
  </v-container>

  <Teleport
    :to="`#${display.xs.value ? 'xs' : 'sm'}-${id}`"
    defer
  >
    <v-container class="px-0">
      <v-row>
        <v-col>
          <SeriesViewActions :series="series" />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <div class="d-flex ga-2">
            <v-chip
              v-if="series.metadata.status"
              size="small"
              rounded
              label
              :text="$formatMessage(seriesStatusMessages[series.metadata.status as SeriesStatus])"
            />
            <v-chip
              v-if="series.metadata.language"
              size="small"
              rounded
              label
              :text="languageDisplayNames.of(series.metadata.language)"
            />
            <v-chip
              v-if="series.metadata.ageRating"
              size="small"
              rounded
              label
              :text="
                $formatMessage(
                  {
                    description: 'Series view: age rating chip',
                    defaultMessage: '{rating}+',
                    id: 'rnClur',
                  },
                  { rating: series.metadata.ageRating },
                )
              "
            />
            <v-chip
              v-if="series.metadata.readingDirection"
              size="small"
              rounded
              label
              :text="
                $formatMessage(
                  readingDirectionMessages[series.metadata.readingDirection as ReadingDirection],
                )
              "
            />
            <v-chip
              v-if="series.metadata.bookUnit"
              size="small"
              rounded
              label
              :text="$formatMessage(bookUnitMessages[series.metadata.bookUnit as BookUnit])"
            />
          </div>
        </v-col>
      </v-row>

      <v-row
        v-if="series.deleted"
        density="compact"
      >
        <v-col>
          <v-alert
            type="error"
            variant="tonal"
            :text="
              $formatMessage({
                description: 'Series view: files deleted',
                defaultMessage: 'The series files could not be found',
                id: 'Ku7NJ+',
              })
            "
          />
        </v-col>
      </v-row>

      <v-row v-if="series.metadata.summary || series.booksMetadata.summary">
        <v-col>
          <div
            v-if="!series.metadata.summary && series.booksMetadata.summary"
            class="text-label-medium"
          >
            {{
              $formatMessage(
                {
                  description: 'Series view: summary from book label',
                  defaultMessage: 'Summary from book {number}:',
                  id: 'MEWT0D',
                },
                { number: series.booksMetadata.summaryNumber },
              )
            }}
          </div>
          <ReadMore :text="series.metadata.summary || series.booksMetadata.summary" />
        </v-col>
      </v-row>

      <v-row v-if="tableRows.length > 0">
        <v-col>
          <SimpleDataTable :rows="tableRows" />
        </v-col>
      </v-row>

      <v-row density="compact">
        <v-col cols="auto">
          <v-btn
            variant="text"
            size="small"
            :text="
              $formatMessage({
                description: 'Series view: button to display all series information',
                defaultMessage: 'Show info',
                id: 'C1bGrS',
              })
            "
            @mouseenter="dialogSimple.activator = $event.currentTarget"
            @click="showDialogExtra()"
          />
        </v-col>
      </v-row>
    </v-container>
  </Teleport>
</template>

<script setup lang="ts">
import { seriesPosterUrl } from '@/api/images'

import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import SimpleDataTable, { type TableRow } from '@/components/SimpleDataTable.vue'
import { contributorsRolesMessages } from '@/types/referential'
import { createOrderCompareFn } from '@/functions/sort'
import { useSeries } from '@/composables/series/useSeries'
import { type ReadingDirection, readingDirectionMessages } from '@/types/ReadingDirection'
import { type BookUnit, bookUnitMessages } from '@/types/BookUnit'
import { languageDisplayNames } from '@/utils/i18n/locale-helper'
import { type SeriesStatus, seriesStatusMessages } from '@/types/SeriesStatus'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useBooks } from '@/composables/book/useBooks'
import type { SeriesDto } from '@/generated/openapi'

const intl = useIntl()
const display = useDisplay()
const id = useId()
const posterMaxWidth = 220

const props = defineProps<{
  series: SeriesDto
}>()

const { unreadCount, isRead } = useSeries(() => props.series)
const { getFirstBookInParentQuery } = useBooks(() => props.series)

const { data: booksOnDeck } = getFirstBookInParentQuery(true)
// Komga stores one file as one book whatever the series is divided into, so counting
// them is the same operation either way - only the noun changes. An unset unit means
// the filenames never said, and "book" is the honest word for that.
const bookUnit = computed(() => props.series.metadata.bookUnit || 'BOOK')

// These messages live here rather than in the template because their nested
// select/plural produces "}}" sequences, which end a Vue interpolation early.
const bookCountText = computed(() =>
  props.series.metadata.totalBookCount
    ? intl.formatMessage(
        {
          description: 'Series view: count of books in series with total count',
          id: 'W9WB3s',
          defaultMessage: `{unit, select,
VOLUME {{total, plural, one {{count} / # volume} other {{count} / # volumes}}}
CHAPTER {{total, plural, one {{count} / # chapter} other {{count} / # chapters}}}
ISSUE {{total, plural, one {{count} / # issue} other {{count} / # issues}}}
other {{total, plural, one {{count} / # book} other {{count} / # books}}}
}`,
        },
        {
          count: props.series.booksCount,
          total: props.series.metadata.totalBookCount,
          unit: bookUnit.value,
        },
      )
    : intl.formatMessage(
        {
          description: 'Series view: count of books in series',
          id: 'RYC0Xa',
          defaultMessage: `{unit, select,
VOLUME {{count, plural, one {# volume} other {# volumes}}}
CHAPTER {{count, plural, one {# chapter} other {# chapters}}}
ISSUE {{count, plural, one {# issue} other {# issues}}}
other {{count, plural, one {# book} other {# books}}}
}`,
        },
        { count: props.series.booksCount, unit: bookUnit.value },
      ),
)

const bookOnDeck = computed(() => booksOnDeck.value?.content?.[0])

const alternateTitles = computed(() =>
  props.series.metadata.alternateTitles.map((it) => ({
    header: it.label,
    data: it.title,
  })),
)

const allRows = computed(() => {
  const rows = {} as Record<string, TableRow>

  if (props.series.booksMetadata.authors.length > 0)
    Object.entries(Object.groupBy(props.series.booksMetadata.authors, (it) => it.role))
      .toSorted(createOrderCompareFn(Object.keys(contributorsRolesMessages), ([role]) => role))
      .forEach(([role, contributor]) => {
        rows[role] = {
          header: contributorsRolesMessages?.[role]
            ? intl.formatMessage(contributorsRolesMessages?.[role])
            : role,
          data: contributor!.map((it) => ({ text: it.name })),
        }
      })

  if (props.series.metadata.publisher)
    rows['publisher'] = {
      header: intl.formatMessage({
        description: 'Series view table: publisher header',
        defaultMessage: 'Publisher',
        id: 'OLqBQc',
      }),
      data: [{ text: props.series.metadata.publisher }],
    }

  if (props.series.metadata.alternatePublishers.length > 0)
    rows['alternatePublishers'] = {
      header: intl.formatMessage({
        description: 'Series view table: alternate publishers header',
        defaultMessage: 'Other publishers',
        id: '67VpHr',
      }),
      data: props.series.metadata.alternatePublishers.map((it) => ({ text: it })),
    }

  if (props.series.metadata.serialization)
    rows['serialization'] = {
      header: intl.formatMessage({
        description: 'Series view table: serialization header',
        defaultMessage: 'Serialization',
        id: 'e/YJO6',
      }),
      data: [{ text: props.series.metadata.serialization }],
    }

  if (props.series.metadata.score != null)
    rows['score'] = {
      header: intl.formatMessage({
        description: 'Series view table: score header',
        defaultMessage: 'Score',
        id: 'eEOGfq',
      }),
      data: [
        { text: intl.formatNumber(props.series.metadata.score, { maximumFractionDigits: 1 }) },
      ],
    }

  if (props.series.metadata.releaseDate)
    rows['releaseDate'] = {
      header: intl.formatMessage({
        description: 'Series view table: release date header',
        defaultMessage: 'Release date',
        id: 'lwtvpH',
      }),
      data: [
        {
          text: intl.formatDate(props.series.metadata.releaseDate, {
            dateStyle: 'medium',
            timeZone: 'UTC',
          }),
        },
      ],
    }

  if (props.series.metadata.genres.length > 0)
    rows['genres'] = {
      header: intl.formatMessage({
        description: 'Series view table: genre header',
        defaultMessage: 'Genre',
        id: 'r5O+/d',
      }),
      data: props.series.metadata.genres.map((it) => ({ text: it })),
    }

  if (props.series.metadata.tags.length > 0)
    rows['tags'] = {
      header: intl.formatMessage({
        description: 'Series view table: tags header',
        defaultMessage: 'Tags',
        id: '6UXlVe',
      }),
      data: props.series.metadata.tags.map((it) => ({ text: it })),
    }
  if (props.series.booksMetadata.tags.length > 0)
    rows['bookTags'] = {
      header: intl.formatMessage({
        description: 'Series view table: book tags header',
        defaultMessage: 'Book tags',
        id: 'Thjcar',
      }),
      data: props.series.booksMetadata.tags.map((it) => ({ text: it })),
    }

  if (props.series.metadata.links.length > 0)
    rows['links'] = {
      header: intl.formatMessage({
        description: 'Series view table: links header',
        defaultMessage: 'Links',
        id: 'fbEqBB',
      }),
      data: props.series.metadata.links.map((it) => ({ text: it.label, href: it.url })),
    }
  rows['filePath'] = {
    header: intl.formatMessage({
      description: 'Series view table: file path header',
      defaultMessage: 'File path',
      id: '+mJGIg',
    }),
    data: props.series.url,
  }
  rows['created'] = {
    header: intl.formatMessage({
      description: 'Series view table: date created header',
      defaultMessage: 'Created',
      id: 'dx9s7S',
    }),
    data: intl.formatDate(props.series.created, { dateStyle: 'medium', timeStyle: 'short' }),
  }
  rows['modified'] = {
    header: intl.formatMessage({
      description: 'Series view table: date last modified header',
      defaultMessage: 'Last modified',
      id: 'Y4xJBN',
    }),
    data: intl.formatDate(props.series.lastModified, { dateStyle: 'medium', timeStyle: 'short' }),
  }

  return rows
})

const displayDefault = [
  'writer',
  'penciller',
  'publisher',
  'alternatePublishers',
  'serialization',
  'score',
  'releaseDate',
  'genre',
  'tags',
  'links',
]
const tableRows = computed(() =>
  Object.entries(allRows.value)
    .filter(([key]) => displayDefault.includes(key))
    .map(([, value]) => value),
)

const { simple: dialogSimple } = storeToRefs(useDialogsStore())

function showDialogExtra() {
  dialogSimple.value.dialogProps = {
    fullscreen: display.xs.value,
    scrollable: true,
    maxWidth: 900,
  }
  dialogSimple.value.slot = {
    component: markRaw(SimpleDataTable),
    props: {
      rows: Object.values(allRows.value),
    },
  }
}
</script>
