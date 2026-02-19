import { RecommendKeywordResponseCandidate } from '../../lib/types'

export function KeywordChips({
  candidates,
  onPick,
}: {
  candidates: RecommendKeywordResponseCandidate[]
  onPick: (keyword: string) => void
}) {
  if (!candidates.length) {
    return <p className="muted">추천 키워드가 비어있어요.</p>
  }

  return (
    <div className="keyword-chips">
      <h4>추천 키워드</h4>
      <div className="chips-wrap">
        {candidates.map((item) => (
          <button
            key={item.keyword}
            type="button"
            className="chip"
            onClick={() => onPick(item.keyword)}
          >
            {item.keyword}
          </button>
        ))}
      </div>
    </div>
  )
}
