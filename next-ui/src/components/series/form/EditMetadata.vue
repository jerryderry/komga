<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-select
          v-model="bookUnit"
          :items="bookUnitOptions"
          clearable
          :label="
            $formatMessage({
              description: 'Form edit series metadata: book unit field',
              id: 'Il3plE',
              defaultMessage: 'Book unit',
            })
          "
          :hint="
            $formatMessage({
              description: 'Form edit series metadata: book unit hint',
              id: 'sBxR1U',
              defaultMessage:
                'What one book of this series is. Detected from the filenames on each scan unless locked.',
            })
          "
          persistent-hint
          @update:model-value="model.bookUnitLock = true"
        >
          <template #prepend>
            <v-btn
              :icon="model.bookUnitLock ? 'i-mdi:lock' : 'i-mdi:lock-open-variant'"
              :color="model.bookUnitLock ? 'secondary' : undefined"
              variant="text"
              density="comfortable"
              :aria-label="
                model.bookUnitLock
                  ? $formatMessage({
                      description: 'Form edit series metadata: unlock field',
                      id: '9B/iRq',
                      defaultMessage: 'Unlock, allowing detection to overwrite this',
                    })
                  : $formatMessage({
                      description: 'Form edit series metadata: lock field',
                      id: 'Nj5JaP',
                      defaultMessage: 'Lock, keeping detection from overwriting this',
                    })
              "
              @click="model.bookUnitLock = !model.bookUnitLock"
            />
          </template>
        </v-select>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { BookUnitValues, bookUnitMessages } from '@/types/BookUnit'
import type { SeriesMetadataDto } from '@/generated/openapi'

const model = defineModel<SeriesMetadataDto>({ required: true })

const intl = useIntl()

const bookUnitOptions = BookUnitValues.map((x) => ({
  title: intl.formatMessage(bookUnitMessages[x]),
  value: x,
}))

// The read DTO spells "unset" as an empty string, which is not one of the select's
// options and would leave the field showing a blank entry rather than its placeholder.
const bookUnit = computed({
  get: () => model.value.bookUnit || null,
  set: (value) => (model.value.bookUnit = value ?? ''),
})
</script>
