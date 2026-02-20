import { FormEvent, useEffect, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { cardApi, recommendApi } from '../lib/api'
import { createRecommendEvent } from '../lib/api'
import { toAppError } from '../lib/error'
import { storage } from '../lib/storage'
import { LoadingCard } from '../components/LoadingCard'
import { ErrorCard } from '../components/ErrorCard'
import { KeywordChips } from '../features/recommend/KeywordChips'

const userId = 'local-user'

export function CreatePage() {
  const [keyword, setKeyword] = useState('')
  const [tone, setTone] = useState('objective')
  const [categoryId, setCategoryId] = useState('tech')
  const navigate = useNavigate()
  const [researchMode, setResearchMode] = useState(false)

  const {
    data: recommendData,
    isLoading: isRecommendLoading,
    refetch: loadRecommendations,
  } = useQuery({
    queryKey: ['recommend-keywords', keyword, categoryId],
    enabled: keyword.length >= 2 || !!categoryId,
    staleTime: 30_000,
    queryFn: () =>
      recommendApi.keywords({
        userId,
        currentQuery: keyword,
        categoryId: categoryId,
        limit: 5,
      }),
  })

  const categories = [
    { id: 'tech', label: 'IT/테크', icon: '💻' },
    { id: 'health', label: '건강/푸드', icon: '🥗' },
    { id: 'finance', label: '경제/금융', icon: '📈' },
    { id: 'culture', label: '문화/생활', icon: '🎨' },
  ]

  const cardMut = useMutation({
    mutationFn: () =>
      cardApi.generate({
        keyword,
        tone,
        mode: researchMode ? 'deep' : 'quick',
      }),
    onSuccess: (res) => {
      storage.saveCard(res)
      navigate(`/cards/${res.id}`)
      recommendApi.events({
        userId,
        events: [createRecommendEvent(keyword)],
      })
    },
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!keyword.trim()) return

    cardMut.mutate()
  }

  const cardError = cardMut.error ? toAppError(cardMut.error as unknown) : null

  useEffect(() => {
    if (!keyword) return
    const t = window.setTimeout(() => {
      loadRecommendations()
    }, 500)
    return () => window.clearTimeout(t)
  }, [keyword, loadRecommendations])

  if (cardMut.isPending) {
    return (
      <div style={{ display: 'grid', gap: 'var(--space-md)', padding: 'var(--space-xl) 0' }}>
        <LoadingCard label={researchMode ? '딥 리서치 근거를 수집해 카드를 생성 중입니다...' : '빠른 요약 카드 생성 중입니다...'} />
        <p className="muted" style={{ textAlign: 'center', animation: 'pulse 2s infinite' }}>잠시만 기다려주세요. 약 10-20초 정도 소요될 수 있습니다.</p>
        <style>{`
          @keyframes pulse {
            0% { opacity: 0.5; }
            50% { opacity: 1; }
            100% { opacity: 0.5; }
          }
        `}</style>
      </div>
    )
  }

  return (
    <div style={{ animation: 'fadeIn 0.4s ease-out' }}>
      <header style={{ marginBottom: 'var(--space-xl)' }}>
        <button className="muted" onClick={() => navigate('/')} style={{ background: 'none', padding: 0, minHeight: 'auto', marginBottom: 'var(--space-md)' }}>
          ← 뒤로 가기
        </button>
        <h2 style={{ fontSize: '32px' }}>이슈 요약 생성</h2>
        <p className="muted">아무 키워드나 입력하세요. AI가 최신 이슈를 3장으로 요약합니다.</p>
      </header>

      {cardError && (
        <div style={{ marginBottom: 'var(--space-lg)' }}>
          <ErrorCard
            error={cardError}
            onRetry={() => {
              cardMut.reset()
              cardMut.mutate()
            }}
          />
        </div>
      )}

      <form onSubmit={handleSubmit} className="card-form">
        <div className="field">
          <label>관심 카테고리</label>
          <div className="chips-wrap" style={{ gap: 'var(--space-sm)' }}>
            {categories.map((cat) => (
              <button
                key={cat.id}
                type="button"
                className="chip"
                style={{ 
                  background: categoryId === cat.id ? 'var(--color-main)' : 'var(--color-bg)',
                  color: categoryId === cat.id ? '#fff' : 'var(--color-text)',
                  border: '1px solid var(--color-border)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  minHeight: '36px'
                }}
                onClick={() => setCategoryId(cat.id)}
              >
                <span>{cat.icon}</span>
                <span>{cat.label}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="field">
          <label>키워드 입력</label>
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="예: 엔비디아 주가, 뉴진스 컴백, 비트코인"
            autoFocus
          />
        </div>

        {keyword.length >= 2 && (
          <div className="keyword-chips-section" style={{ marginBottom: 'var(--space-lg)' }}>
            {isRecommendLoading ? (
              <p className="muted" style={{ fontSize: '12px' }}>최신 연관어 탐색 중...</p>
            ) : (
              <KeywordChips
                candidates={recommendData?.candidates ?? []}
                onPick={(k) => setKeyword(k)}
              />
            )}
          </div>
        )}

        <div className="field">
          <label>뉴스 스타일</label>
          <div className="row" style={{ gap: 'var(--space-sm)' }}>
            {['objective', 'insightful', 'witty', 'summary'].map((t) => (
              <button
                key={t}
                type="button"
                className={tone === t ? 'primary' : 'secondary'}
                style={{ 
                  flex: 1, 
                  fontSize: '13px', 
                  minHeight: '40px',
                  background: tone === t ? 'var(--color-main)' : 'var(--color-bg)',
                  border: tone === t ? 'none' : '1px solid var(--color-border)',
                  color: tone === t ? '#fff' : 'var(--color-text)'
                }}
                onClick={() => setTone(t)}
              >
                {t === 'objective' ? '객관적' : t === 'insightful' ? '통찰력' : t === 'witty' ? '위트있는' : '핵심요약'}
              </button>
            ))}
          </div>
        </div>

        <div className="card" style={{ background: 'var(--color-sub-soft)', border: 'none', margin: 'var(--space-md) 0' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', userSelect: 'none' }}>
            <input
              type="checkbox"
              style={{ width: '20px', height: '20px' }}
              checked={researchMode}
              onChange={(e) => setResearchMode(e.target.checked)}
            />
            <div>
              <strong style={{ display: 'block', fontSize: '15px' }}>딥 리서치 모드 활성화</strong>
              <span className="muted" style={{ fontSize: '12px' }}>체크 시 근거/팩트체크/리스크가 포함된 카드로 생성됩니다(시간 더 소요).</span>
            </div>
          </label>
        </div>

        <div style={{ marginTop: 'var(--space-xl)' }}>
          <button className="primary" type="submit" style={{ width: '100%', height: '56px', fontSize: '18px' }} disabled={!keyword.trim()}>
            AI 카드 생성하기
          </button>
        </div>
      </form>
      
          <style>{`
            @keyframes fadeIn {
              from { opacity: 0; }
              to { opacity: 1; }
            }
          `}</style>
      
    </div>
  )
}
