import { ResearchItemCard } from './ResearchItemCard'
import type { ResearchRunResponse } from '../../lib/types'

function FactcheckClaims({ claims }: { claims: Array<{ claimText: string; verdict: string; evidenceIds: string[] }> }) {
  if (!claims.length) return null

  return (
    <ul className="research-claims">
      {claims.map((claim) => (
        <li key={claim.claimText}>
          <strong>{claim.verdict}</strong> {claim.claimText}
          <span className="muted"> ({claim.evidenceIds.join(', ')})</span>
        </li>
      ))}
    </ul>
  )
}

export function ResearchResultView({ result }: { result: ResearchRunResponse }) {
  return (
    <div className="research-result-grid">
      <section className="card">
        <h3>요약</h3>
        <p>{result.summary.brief}</p>
        <p className="muted">{result.summary.analystNote}</p>
        <div className="research-badges">
          <span className="badge">조회시간: {result.generatedAt}</span>
          {result.usage ? <span className="badge">캐시: {result.usage.cacheHit ? 'HIT' : 'MISS'}</span> : null}
        </div>
        {result.summary.riskFlags.length ? (
          <p className="muted">리스크: {result.summary.riskFlags.join(', ')}</p>
        ) : null}
      </section>

      <section>
        <h3>신뢰성(팩트체크)</h3>
        <p className="muted">상태: {result.items[0]?.factcheck.status}</p>
        <FactcheckClaims claims={result.items[0]?.factcheck.claims ?? []} />
      </section>

      <section>
        <h3>검색 결과</h3>
        <div className="research-list">
          {result.items.map((item) => (
            <ResearchItemCard key={item.itemId} item={item} />
          ))}
        </div>
      </section>
    </div>
  )
}
