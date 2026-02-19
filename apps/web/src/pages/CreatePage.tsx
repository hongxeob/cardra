import { FormEvent, useEffect, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { cardApi, recommendApi, researchApi } from '../lib/api'
import { createRecommendEvent } from '../lib/api'
import { toAppError } from '../lib/error'
import { LoadingCard } from '../components/LoadingCard'
import { ErrorCard } from '../components/ErrorCard'
import { KeywordChips } from '../features/recommend/KeywordChips'

const userId = 'local-user'

export function CreatePage() {
  const [keyword, setKeyword] = useState('')
  const [tone, setTone] = useState('minimal')
  const navigate = useNavigate()
  const [researchMode, setResearchMode] = useState(false)

  const {
    data: recommendData,
    isLoading: isRecommendLoading,
    refetch: loadRecommendations,
  } = useQuery({
    queryKey: ['recommend-keywords', keyword],
    enabled: keyword.length >= 2,
    staleTime: 30_000,
    queryFn: () =>
      recommendApi.keywords({
        userId,
        currentQuery: keyword,
        limit: 5,
      }),
  })

  const cardMut = useMutation({
    mutationFn: () => cardApi.generate({ keyword, tone }),
    onSuccess: (res) => {
      navigate(`/cards/${res.id}`)
      recommendApi.events({
        userId,
        events: [createRecommendEvent(keyword)],
      })
    },
  })

  const researchMut = useMutation({
    mutationFn: () =>
      researchApi.createJob({
        keyword,
        timeRange: '24h',
      }),
    onSuccess: (res) => {
      if (researchMode) {
        navigate(`/research/loading/${res.jobId}`)
      }
    },
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!keyword.trim()) return

    cardMut.mutate()
    if (researchMode) {
      researchMut.mutate()
    }
  }

  const cardError = cardMut.error ? toAppError(cardMut.error as unknown) : null

  useEffect(() => {
    if (!keyword) return
    const t = window.setTimeout(() => {
      loadRecommendations()
    }, 500)
    return () => window.clearTimeout(t)
  }, [keyword, loadRecommendations])

  if (cardMut.isPending || (researchMode && researchMut.isPending)) {
    return (
      <div style={{ display: 'grid', gap: 'var(--space-md)', padding: 'var(--space-xl) 0' }}>
        <LoadingCard label={researchMode ? '리서치 데이터를 수집하고 카드를 설계 중입니다...' : 'AI가 카드를 디자인하고 있습니다...'} />
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

  if (cardError && !cardError.retryable) {
    return <ErrorCard error={cardError} onBack={() => navigate('/')} />
  }

  return (
    <div style={{ animation: 'fadeIn 0.4s ease-out' }}>
      <header style={{ marginBottom: 'var(--space-xl)' }}>
        <button className="muted" onClick={() => navigate('/')} style={{ background: 'none', padding: 0, minHeight: 'auto', marginBottom: 'var(--space-md)' }}>
          ← 뒤로 가기
        </button>
        <h2 style={{ fontSize: '32px' }}>콘텐츠 생성</h2>
        <p className="muted">키워드를 입력하면 AI가 최적의 카드뉴스를 구성합니다.</p>
      </header>

      <form onSubmit={handleSubmit} className="card-form">
        <div className="field">
          <label>주제 및 키워드</label>
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="예: 2026 AI 트렌드, 건강한 식습관"
            autoFocus
          />
        </div>

        {keyword.length >= 2 && (
          <div className="keyword-chips-section" style={{ marginBottom: 'var(--space-lg)' }}>
            {isRecommendLoading ? (
              <p className="muted" style={{ fontSize: '12px' }}>연관 키워드 탐색 중...</p>
            ) : (
              <KeywordChips
                candidates={recommendData?.candidates ?? []}
                onPick={(k) => setKeyword(k)}
              />
            )}
          </div>
        )}

        <div className="field">
          <label>스타일 톤</label>
          <div className="row" style={{ gap: 'var(--space-sm)' }}>
            {['minimal', 'professional', 'cheerful', 'cyberpunk'].map((t) => (
              <button
                key={t}
                type="button"
                className={tone === t ? 'primary' : 'secondary'}
                style={{ 
                  flex: 1, 
                  fontSize: '14px', 
                  minHeight: '40px',
                  background: tone === t ? 'var(--color-main)' : 'var(--color-bg)',
                  border: tone === t ? 'none' : '1px solid var(--color-border)',
                  color: tone === t ? '#fff' : 'var(--color-text)'
                }}
                onClick={() => setTone(t)}
              >
                {t.charAt(0).toUpperCase() + t.slice(1)}
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
              <span className="muted" style={{ fontSize: '12px' }}>웹상의 실시간 데이터를 분석하여 더 정확한 근거를 포함합니다.</span>
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
