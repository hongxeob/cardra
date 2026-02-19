import type { CardItem } from '../../lib/types'

function containerClass(item: CardItem) {
  const base = 'card card-item'
  const variant = item.variant ? `variant-${item.variant}` : 'variant-default'
  const ratio = item.style?.layout === 'wide' ? 'card-item--square' : 'card-item--portrait'
  return `${base} ${variant} ${ratio}`
}

export function CardItemBlock({ item }: { item: CardItem }) {
  const { media, cta, tags, source, sourceAt } = item

  return (
    <article className={containerClass(item)} style={{ position: 'relative' }}>
      <header>
        {tags?.length ? (
          <div className="chips-wrap" style={{ marginBottom: 'var(--space-sm)' }}>
            <span className="badge" style={{ background: 'var(--color-main-soft)', color: 'var(--color-main)', border: 'none', fontWeight: 800 }}>
              {tags[0].toUpperCase()}
            </span>
          </div>
        ) : null}
        <h3 className="card-title">{item.title}</h3>
      </header>

      <section className="card-body">
        <p>{item.body}</p>
      </section>

      {media?.imageUrl ? (
        <figure className="card__media" role="presentation">
          <img src={media.imageUrl} alt={media.altText || item.title} />
        </figure>
      ) : (
        item.imageHint && (
          <div className="card__media" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 'var(--space-lg)', textAlign: 'center', background: 'var(--color-bg)', border: '1px dashed var(--color-border)' }}>
            <p className="muted" style={{ fontStyle: 'italic' }}>Visual Idea: {item.imageHint}</p>
          </div>
        )
      )}

      {tags && tags.length > 1 && (
        <div className="chips-wrap" style={{ marginTop: 'var(--space-sm)' }}>
          {tags.slice(1).map((tag) => (
            <span key={tag} className="chip">
              #{tag}
            </span>
          ))}
        </div>
      )}

      <footer style={{ marginTop: 'auto', paddingTop: 'var(--space-lg)', display: 'grid', gap: 'var(--space-md)' }}>
        {cta && cta.target && (
          <button
            className="primary"
            style={{ width: '100%' }}
            onClick={() => {
              if (cta.actionType === 'open') {
                window.open(cta.target, '_blank')
              }
            }}
          >
            {cta.label || '자세히 보기'}
          </button>
        )}

        <div className="row" style={{ justifyContent: 'space-between', borderTop: '1px solid var(--color-border)', paddingTop: 'var(--space-sm)' }}>
          <p className="muted">© {source.join(', ')}</p>
          <p className="muted">{sourceAt}</p>
        </div>
      </footer>
    </article>
  )
}
