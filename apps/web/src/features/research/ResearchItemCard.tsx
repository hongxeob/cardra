import type { ResearchItem } from '../../lib/types'

export function ResearchItemCard({ item }: { item: ResearchItem }) {
  return (
    <article className="card research-item" style={{ borderLeft: '4px solid var(--color-sub)', overflowWrap: 'break-word', maxWidth: '100%' }}>
      <h4 style={{ fontSize: '18px', marginBottom: 'var(--space-xs)', wordBreak: 'break-word' }}>{item.title}</h4>
      <p className="card-body" style={{ fontSize: '14px', marginBottom: 'var(--space-sm)', wordBreak: 'break-word' }}>{item.snippet}</p>
      
      <section className="research-meta" style={{ display: 'grid', gap: '2px', marginBottom: 'var(--space-sm)' }}>
        <p className="muted" style={{ fontWeight: 600 }}>출처: {item.source.publisher}</p>
        <p className="muted" style={{ fontSize: '11px', opacity: 0.7, wordBreak: 'break-all' }}>{item.source.url}</p>
      </section>

      <div className="research-badges" style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'flex-start' }}>
        <div style={{ width: '100%', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ flex: 1, height: '6px', background: 'var(--color-bg)', borderRadius: '3px', overflow: 'hidden' }}>
            <div style={{ 
              width: `${(item.factcheck.confidence * 100)}%`, 
              height: '100%', 
              background: 'var(--color-main)',
              transition: 'width 1s ease-out'
            }}></div>
          </div>
          <span className="muted" style={{ fontSize: '11px', fontWeight: 700 }}>{(item.factcheck.confidence * 100).toFixed(0)}%</span>
        </div>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <span className="badge" style={{ 
            background: item.factcheck.status === 'verified' ? '#E6F9F3' : '#FFF7ED',
            color: item.factcheck.status === 'verified' ? '#065F46' : '#9A3412',
            border: 'none',
            fontSize: '10px'
          }}>
            {item.factcheck.status.toUpperCase()}
          </span>
          <span className="badge" style={{ background: 'var(--color-main-soft)', color: 'var(--color-main)', border: 'none', fontSize: '10px' }}>
            Trend {item.trend.trendScore}
          </span>
        </div>
      </div>
    </article>
  )
}
