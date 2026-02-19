import type { CardItem } from '../../lib/types'

function variantClass(variant?: string) {
  switch (variant) {
    case 'headline':
      return 'card card-item variant-headline'
    case 'insight':
      return 'card card-item variant-insight'
    case 'summary':
      return 'card card-item variant-summary'
    default:
      return 'card card-item'
  }
}

function layoutClass(layout?: string) {
  if (layout === 'wide') return 'wide'
  if (layout === 'compact') return 'compact'
  return ''
}

export function CardItemBlock({ item }: { item: CardItem }) {
  const style = item.style

  return (
    <article className={variantClass(item.variant)} data-layout={layoutClass(style?.layout)}>
      <h3 className="card-title">{item.title}</h3>
      <p className="card-body">{item.body}</p>

      {style ? (
        <p className="muted">
          tone: {style.tone} / {style.layout} / {style.emphasis}
        </p>
      ) : null}

      {item.media?.imageUrl ? (
        <img
          src={item.media.imageUrl}
          alt={item.media.altText || item.title}
          style={{ width: '100%', borderRadius: 12, margin: '12px 0' }}
        />
      ) : (
        item.imageHint && <p className="muted">이미지 힌트: {item.imageHint}</p>
      )}

      {item.tags?.length ? (
        <div className="chips-wrap" style={{ marginBottom: 8 }}>
          {item.tags.map((tag) => (
            <span key={tag} className="chip">
              #{tag}
            </span>
          ))}
        </div>
      ) : null}

      {item.cta && (
        <div>
          <button
            className="secondary"
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

      <p className="muted">출처: {item.source.join(', ')}</p>
      <p className="muted">{item.sourceAt}</p>
    </article>
  )
}
