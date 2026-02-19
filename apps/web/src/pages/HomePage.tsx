import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { healthApi, uiApi } from '../lib/api'

function HealthBadge({ health }: { health: 'ok' | 'bad' | 'loading' }) {
  const label = health === 'ok' ? '정상' : health === 'bad' ? '점검 필요' : '체크중'
  const tone = health === 'ok' ? 'primary' : health === 'bad' ? 'error' : 'muted'

  return <span className={`health-badge health-${tone}`}>{`시스템 ${label}`}</span>
}

type QuickAction = {
  key: string
  to: string
  label: string
  detail: string
}

const routeByApiPath: Record<string, QuickAction> = {
  '/api/v1/cards/generate': { key: 'cards-create', to: '/create', label: '카드 생성', detail: '카드 생성 페이지' },
  '/api/v1/cards/{id}': { key: 'cards-detail', to: '/cards/sample', label: '카드 상세', detail: '최근 생성 카드로 이동(샘플)' },
  '/api/v1/research/run': { key: 'research-run', to: '/create', label: '연구 실행', detail: '리서치 모드로 카드 생성' },
  '/api/v1/research/jobs/{jobId}': { key: 'research-status', to: '/create', label: '리서치 상태', detail: '실행 후 상태 페이지(샘플)' },
  '/api/v1/research/jobs/{jobId}/result': { key: 'research-result', to: '/create', label: '리서치 결과', detail: '연구 결과 페이지(샘플)' },
  '/api/v1/research/jobs/{jobId}/cancel': { key: 'research-cancel', to: '/create', label: '리서치 취소', detail: '진행중 작업 취소' },
  '/api/v1/recommend/keywords': { key: 'recommend', to: '/create', label: '키워드 추천', detail: '입력 기반 추천 받아보기' },
  '/api/v1/recommend/events': { key: 'recommend-events', to: '/create', label: '이벤트 수집', detail: '추천 히스토리 반영' },
}

export function HomePage() {
  const { data: contractData } = useQuery({
    queryKey: ['ui-contracts'],
    queryFn: uiApi.contracts,
    staleTime: 60_000,
    retry: false,
  })

  const quickActions = useMemo<QuickAction[]>(() => {
    if (!contractData?.routes?.length) {
      return [
        { key: 'default-create', to: '/create', label: '새 카드 만들기', detail: '키워드로 카드 생성하기' },
        { key: 'default-research', to: '/create', label: '리서치 시작', detail: '키워드 기반 리서치 실행' },
      ]
    }

    const base = contractData.routes
      .map((route) => routeByApiPath[route.path])
      .filter(Boolean) as QuickAction[]

    if (base.length) return base

    return [
      { key: 'default-create', to: '/create', label: '새 카드 만들기', detail: '키워드로 카드 생성하기' },
      { key: 'default-research', to: '/create', label: '리서치 시작', detail: '키워드 기반 리서치 실행' },
    ]
  }, [contractData])

  const [health, setHealth] = useState<'ok' | 'bad' | 'loading'>('loading')

  useEffect(() => {
    healthApi
      .ping()
      .then(() => setHealth('ok'))
      .catch(() => setHealth('bad'))
  }, [])

  return (
    <section>
      <header className="home-hero">
        <p className="home-eyebrow">AI 카드 추천 서비스</p>
        <h1>Cardra</h1>
        <p>키워드로 빠르게 카드뉴스를 만들고 리서치까지 바로 연결하세요.</p>
        <HealthBadge health={health} />
      </header>

      <div className="card home-actions">
        {quickActions.map((action) => (
          <Link key={action.key} to={action.to} className="home-link">
            <button className="primary" type="button">
              <span>{action.label}</span>
              <span className="home-action-detail">{action.detail}</span>
            </button>
          </Link>
        ))}
      </div>
    </section>
  )
}
