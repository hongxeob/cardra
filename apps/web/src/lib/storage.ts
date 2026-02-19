import { CardResponse } from './types'

const STORAGE_KEY = 'cardra_recent_cards'

export const storage = {
  saveCard: (card: CardResponse) => {
    const existing = storage.getRecentCards()
    const updated = [card, ...existing.filter(c => c.id !== card.id)].slice(0, 10)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated))
  },

  getRecentCards: (): CardResponse[] => {
    try {
      const data = localStorage.getItem(STORAGE_KEY)
      return data ? JSON.parse(data) : []
    } catch {
      return []
    }
  }
}
