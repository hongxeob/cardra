import { ResearchItemCard } from './ResearchItemCard'
import type { ResearchRunResponse } from '../../lib/types'

function FactcheckClaims({ claims }: { claims: Array<{ claimText: string; verdict: string; evidenceIds: string[] }> }) {
  if (!claims.length) return null

  return (
    <ul className="research-claims" style={{ listStyle: 'none', padding: 0, display: 'grid', gap: 'var(--space-sm)' }}>
      {claims.map((claim) => (
        <li key={claim.claimText} style={{ padding: 'var(--space-sm)', background: 'var(--color-bg)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)' }}>
          <div style={{ display: 'flex', gap: 'var(--space-sm)', alignItems: 'center', marginBottom: '4px' }}>
            <span className="badge" style={{ 
              background: claim.verdict === 'True' ? 'var(--color-success)' : 'var(--color-warning)',
              color: '#fff',
              border: 'none',
              fontSize: '10px'
            }}>
              {claim.verdict}
            </span>
            <strong style={{ fontSize: '14px' }}>{claim.claimText}</strong>
          </div>
          <p className="muted" style={{ fontSize: '12px' }}>Evidence: {claim.evidenceIds.join(', ')}</p>
        </li>
      ))}
    </ul>
  )
}

export function ResearchResultView({ result }: { result: ResearchRunResponse }) {
  return (
    <div className="research-result-grid" style={{ display: 'grid', gap: 'var(--space-lg)' }}>
      <section className="card" style={{ 
        background: 'linear-gradient(135deg, var(--color-main) 0%, #008f65 100%)', 
        color: '#fff',
        border: 'none',
        boxShadow: '0 10px 20px rgba(0, 166, 118, 0.15)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--space-md)' }}>
           <span style={{ fontSize: '24px' }}>📝</span>
           <h3 style={{ color: '#fff', fontSize: '20px' }}>오늘의 이슈 핵심 요약</h3>
        </div>
        <p style={{ fontSize: '17px', lineHeight: 1.7, marginBottom: 'var(--space-md)', fontWeight: 500 }}>{result.summary.brief}</p>
        <div style={{ padding: 'var(--space-md)', background: 'rgba(255,255,255,0.15)', borderRadius: 'var(--radius-sm)', fontSize: '14px', border: '1px solid rgba(255,255,255,0.2)' }}>
          <strong style={{ opacity: 0.9 }}>AI Analyst Note:</strong><br/>
          <span style={{ opacity: 0.95 }}>{result.summary.analystNote}</span>
        </div>
        <div className="research-badges" style={{ marginTop: 'var(--space-md)' }}>
          <span className="badge" style={{ background: 'rgba(255,255,255,0.2)', color: '#fff', border: 'none' }}>
            {result.generatedAt}
          </span>
          {result.usage?.cacheHit && (
            <span className="badge" style={{ background: 'rgba(255,255,255,0.2)', color: '#fff', border: 'none' }}>
              CACHED
            </span>
          )}
        </div>
      </section>

      {result.summary.riskFlags.length > 0 && (
        <section className="card" style={{ borderColor: 'var(--color-error)', background: 'var(--color-error-bg)' }}>
          <h4 className="error" style={{ marginBottom: 'var(--space-sm)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            ⚠️ 주의 요망 (Risk Flags)
          </h4>
          <div className="chips-wrap">
            {result.summary.riskFlags.map(risk => (
              <span key={risk} className="badge" style={{ background: 'var(--color-error)', color: '#fff', border: 'none' }}>
                {risk}
              </span>
            ))}
          </div>
        </section>
      )}

      <section>
        <h3 style={{ marginBottom: 'var(--space-md)', paddingLeft: 'var(--space-xs)', borderLeft: '4px solid var(--color-main)' }}>
          핵심 팩트체크
        </h3>
        <FactcheckClaims claims={result.items[0]?.factcheck.claims ?? []} />
      </section>

      <section>
        <h3 style={{ marginBottom: 'var(--space-md)', paddingLeft: 'var(--space-xs)', borderLeft: '4px solid var(--color-sub)' }}>
          상세 검색 결과
        </h3>
        <div className="research-list" style={{ display: 'grid', gap: 'var(--space-md)' }}>
          {result.items.map((item) => (
            <ResearchItemCard key={item.itemId} item={item} />
          ))}
        </div>
      </section>
    </div>
  )
}
