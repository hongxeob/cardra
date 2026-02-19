import { RecommendKeywordResponseCandidate } from '../../lib/types'

export function KeywordChips({
  candidates,
  onPick,
}: {
  candidates: RecommendKeywordResponseCandidate[]
  onPick: (keyword: string) => void
}) {
  if (!candidates.length) {
    return null
  }

  return (
    <div className="keyword-chips">
      <h4 style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginBottom: 'var(--space-sm)' }}>추천 검색어</h4>
      <div className="chips-wrap">
        {candidates.map((item) => (
          <button
            key={item.keyword}
            type="button"
            className="chip"
            style={{ 
              border: '1px solid var(--color-border)',
              background: 'var(--color-surface)',
              transition: 'all 0.2s ease'
            }}
            onClick={() => onPick(item.keyword)}
          >
            {item.keyword}
          </button>
        ))}
      </div>
    </div>
  )
}
