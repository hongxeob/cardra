import { CardResponse } from './types'

const STORAGE_KEY = 'cardra_recent_cards'
const USER_ID_KEY = 'cardra_user_id'

export const storage = {
  getUserId: (): string => {
    const existing = localStorage.getItem(USER_ID_KEY)
    if (existing) return existing
    const newId = crypto.randomUUID()
    localStorage.setItem(USER_ID_KEY, newId)
    return newId
  },


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
