import type { CardItem } from '../../lib/types'

function classesForVariant(variant?: string) {
  if (variant === 'headline') return 'card'
  if (variant === 'insight') return 'card'
  if (variant === 'summary') return 'card'
  return 'card'
}

export function CardItemBlock({ item }: { item: CardItem }) {
  return (
    <article className={classesForVariant(item.variant)} style={{ marginBottom: 12 }}>
      <h3>{item.title}</h3>
      <p style={{ whiteSpace: 'pre-wrap' }}>{item.body}</p>

      {item.media?.imageUrl ? (
        <img
          src={item.media.imageUrl}
          alt={item.media.altText || item.title}
          style={{ width: '100%', borderRadius: 12, margin: '12px 0' }}
        />
      ) : (
        item.imageHint && <p style={{ fontSize: 12, color: 'var(--color-text-muted)' }}>이미지 힌트: {item.imageHint}</p>
      )}

      {item.tags && item.tags.length > 0 && (
        <div className="row" style={{ flexWrap: 'wrap' }}>
          {item.tags.map((tag) => (
            <span key={tag} className="card" style={{ padding: '2px 8px', fontSize: 12 }}>
              #{tag}
            </span>
          ))}
        </div>
      )}

      {item.cta && (
        <div style={{ marginTop: 8 }}>
          <button
            className="primary"
            onClick={() => {
              if (item.cta?.target) {
                if (item.cta.actionType === 'open') {
                  window.open(item.cta.target, '_blank')
                }
              }
            }}
          >
            {item.cta.label}
          </button>
        </div>
      )}
    </article>
  )
}
