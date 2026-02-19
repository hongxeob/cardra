import type { CardResponse } from '../../lib/types'
import { CardItemBlock } from './CardItemBlock'

export function CardRenderer({ card }: { card: CardResponse }) {
  return (
    <div>
      <h2>{card.keyword}</h2>
      <p>{card.status}</p>
      <div>
        {card.cards.map((item, index) => (
          <CardItemBlock key={`${card.id}-${index}`} item={item} />
        ))}
      </div>
    </div>
  )
}
