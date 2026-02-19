import type { ResearchItem } from '../../lib/types'

export function ResearchItemCard({ item }: { item: ResearchItem }) {
  return (
    <article className="card research-item" style={{ borderLeft: '4px solid var(--color-sub)' }}>
      <h4 style={{ fontSize: '18px', marginBottom: 'var(--space-xs)' }}>{item.title}</h4>
      <p className="card-body" style={{ fontSize: '14px', marginBottom: 'var(--space-sm)' }}>{item.snippet}</p>
      
      <section className="research-meta" style={{ display: 'grid', gap: '2px', marginBottom: 'var(--space-sm)' }}>
        <p className="muted" style={{ fontWeight: 600 }}>출처: {item.source.publisher}</p>
        <p className="muted" style={{ fontSize: '11px', opacity: 0.7, wordBreak: 'break-all' }}>{item.source.url}</p>
      </section>

      <div className="research-badges" style={{ marginTop: 'auto' }}>
        <span className="badge" style={{ 
          background: item.factcheck.status === 'verified' ? '#E6F9F3' : '#FFF7ED',
          color: item.factcheck.status === 'verified' ? '#065F46' : '#9A3412',
          border: 'none'
        }}>
          {item.factcheck.status.toUpperCase()}
        </span>
        <span className="badge">{(item.factcheck.confidence * 100).toFixed(0)}% Confidence</span>
        <span className="badge" style={{ background: 'var(--color-main-soft)', color: 'var(--color-main)', border: 'none' }}>
          Trend {item.trend.trendScore}
        </span>
      </div>
    </article>
  )
}
