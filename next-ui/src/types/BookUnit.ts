import { defineMessages, type MessageDescriptor } from 'vue-intl'

export const BookUnitValues = ['VOLUME', 'CHAPTER', 'ISSUE'] as const

export type BookUnit = (typeof BookUnitValues)[number]

export const bookUnitMessages: Record<BookUnit, MessageDescriptor> = defineMessages({
  VOLUME: {
    description: 'Book unit: VOLUME',
    defaultMessage: 'Volume',
    id: 'enum.BookUnit.VOLUME',
  },
  CHAPTER: {
    description: 'Book unit: CHAPTER',
    defaultMessage: 'Chapter',
    id: 'enum.BookUnit.CHAPTER',
  },
  ISSUE: {
    description: 'Book unit: ISSUE',
    defaultMessage: 'Issue',
    id: 'enum.BookUnit.ISSUE',
  },
})
