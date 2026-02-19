import type { CardItem } from '../../lib/types'

function containerClass(item: CardItem) {
  const base = 'card card-item'
  const variant = item.variant ? `variant-${item.variant}` : 'variant-default'
  const ratio = item.style?.layout === 'wide' ? 'card-item--square' : 'card-item--portrait'
  return `${base} ${variant} ${ratio}`
}

export function CardItemBlock({ item }: { item: CardItem }) {
  const style = item.style

  return (
    <article className={containerClass(item)}>
      <header>
        <h3 className="card-title">{item.title}</h3>
      </header>

      <section className="card-body">
        <p>{item.body}</p>
      </section>

      {style ? (
        <p className="muted" aria-label="card style info">
          {style.tone} / {style.layout} / {style.emphasis}
        </p>
      ) : null}

      {item.media?.imageUrl ? (
        <figure className="card__media" role="presentation">
          <img src={item.media.imageUrl} alt={item.media.altText || item.title} />
        </figure>
      ) : (
        item.imageHint && <p className="muted">이미지 힌트: {item.imageHint}</p>
      )}

      {item.tags?.length ? (
        <div className="chips-wrap" style={{ marginBottom: 8 }}>
          {item.tags.map((tag) => (
            <span key={tag} className="chip" aria-label={`tag ${tag}`}>
              #{tag}
            </span>
          ))}
        </div>
      ) : null}

      {item.cta && item.cta.target && item.cta.actionType === 'open' ? (
        <footer>
          <button
            className="secondary"
            onClick={() => {
              window.open(item.cta?.target, '_blank')
            }}
          >
            {item.cta.label}
          </button>
        </footer>
      ) : null}

      <footer>
        <p className="muted">출처: {item.source.join(', ')}</p>
        <p className="muted">{item.sourceAt}</p>
      </footer>
    </article>
  )
}
