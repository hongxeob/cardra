import { FormEvent, useEffect, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { cardApi, recommendApi, researchApi } from '../lib/api'
import { createRecommendEvent } from '../lib/api'
import { toAppError } from '../lib/error'
import { LoadingCard } from '../components/LoadingCard'
import { ErrorCard } from '../components/ErrorCard'
import { KeywordChips } from '../features/recommend/KeywordChips'

const userId = 'local-user'

export function CreatePage() {
  const [keyword, setKeyword] = useState('')
  const [tone, setTone] = useState('neutral')
  const navigate = useNavigate()
  const [message, setMessage] = useState('')
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
    onError: () => {
      setMessage('카드 생성 요청을 처리하지 못했습니다.')
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
    if (!keyword.trim()) {
      return
    }

    cardMut.mutate()
    if (researchMode) {
      researchMut.mutate()
    }
  }

  const handleRetryCard = () => {
    if (!cardMut.isPending) {
      cardMut.mutate()
    }
  }

  const cardError = cardMut.error ? toAppError(cardMut.error as unknown) : null

  useEffect(() => {
    if (!keyword) {
      return
    }
    const t = window.setTimeout(() => {
      loadRecommendations()
    }, 500)
    return () => window.clearTimeout(t)
  }, [keyword, loadRecommendations])

  const onPickRecommend = (next: string) => {
    setKeyword(next)
  }

  if (cardMut.isPending) {
    return <LoadingCard label="카드를 생성하고 있습니다." />
  }

  if (cardError && !cardError.retryable) {
    return <ErrorCard error={cardError} onBack={() => navigate('/')} />
  }

  return (
    <div>
      <h2>카드 생성</h2>
      <form onSubmit={handleSubmit} className="card-form">
        <div className="field">
          <label>키워드</label>
          <div className="row">
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="예: AI 트렌드"
            />
            {keyword && (
              <button type="button" className="secondary" onClick={() => loadRecommendations()}>
                추천 새로고침
              </button>
            )}
          </div>
        </div>
        <div className="field">
          <label>톤</label>
          <input value={tone} onChange={(e) => setTone(e.target.value)} />
        </div>

        {isRecommendLoading ? (
          <LoadingCard label="추천 키워드 조회 중..." />
        ) : (
          <KeywordChips
            candidates={recommendData?.candidates ?? []}
            onPick={onPickRecommend}
          />
        )}

        <label style={{ display: 'block', marginTop: 8 }}>
          <input
            type="checkbox"
            checked={researchMode}
            onChange={(e) => setResearchMode(e.target.checked)}
          />
          리서치 결과도 함께 실행
        </label>

        <div className="row" style={{ marginTop: 12 }}>
          <button className="primary" type="submit" disabled={cardMut.isPending}>
            {cardMut.isPending ? '생성중...' : '생성'}
          </button>
          <Link to="/">
            <button type="button" className="secondary">
              취소
            </button>
          </Link>
          {cardError && cardError.retryable ? (
            <button className="secondary" type="button" onClick={handleRetryCard}>
              다시 시도
            </button>
          ) : null}
        </div>
        {message ? <p className="error">{message}</p> : null}
        {cardError ? (
          <p className="muted">
            {cardError.retryable && cardError.retryAfter
              ? `${cardError.retryAfter}초 뒤 재시도 가능`
              : ''}
          </p>
        ) : null}
      </form>
    </div>
  )
}
