import type { ResearchItem } from '../../lib/types'

export function ResearchItemCard({ item }: { item: ResearchItem }) {
  return (
    <article className="card research-item">
      <h4>{item.title}</h4>
      <p>{item.snippet}</p>
      <section className="research-meta">
        <p className="muted">출처: {item.source.publisher}</p>
        <p className="muted">{item.source.url}</p>
      </section>

      <div className="research-badges">
        <span className="badge">factcheck: {item.factcheck.status}</span>
        <span className="badge">confidence: {(item.factcheck.confidence * 100).toFixed(1)}%</span>
        <span className="badge">trend: {item.trend.trendScore}</span>
      </div>
    </article>
  )
}
