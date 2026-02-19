import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { healthApi, uiApi } from '../lib/api'

function HealthBadge({ health }: { health: 'ok' | 'bad' | 'loading' }) {
  const label = health === 'ok' ? 'SYSTEM OK' : health === 'bad' ? 'MAINTENANCE' : 'CHECKING'
  const color = health === 'ok' ? 'var(--color-success)' : health === 'bad' ? 'var(--color-error)' : 'var(--color-text-muted)'
  const bg = health === 'ok' ? 'var(--color-main-soft)' : health === 'bad' ? 'var(--color-error-bg)' : '#F3F4F6'

  return (
    <span className="badge" style={{ background: bg, color: color, border: 'none', fontWeight: 700, letterSpacing: '0.05em' }}>
      {label}
    </span>
  )
}

type QuickAction = {
  key: string
  to: string
  label: string
  detail: string
  icon?: string
}

const routeByApiPath: Record<string, QuickAction> = {
  '/api/v1/cards/generate': { key: 'cards-create', to: '/create', label: '카드 만들기', detail: '키워드로 AI 카드뉴스를 생성합니다.', icon: '✨' },
  '/api/v1/research/run': { key: 'research-run', to: '/create', label: '딥 리서치', detail: '실시간 데이터를 분석하여 리포트를 작성합니다.', icon: '🔍' },
}

export function HomePage() {
  const { data: contractData } = useQuery({
    queryKey: ['ui-contracts'],
    queryFn: uiApi.contracts,
    staleTime: 60_000,
    retry: false,
  })

  const quickActions = useMemo<QuickAction[]>(() => {
    const defaultActions = [
      { key: 'default-create', to: '/create', label: '카드 만들기', detail: '키워드로 AI 카드뉴스를 생성합니다.', icon: '✨' },
      { key: 'default-research', to: '/create', label: '딥 리서치', detail: '실시간 데이터를 분석하여 리포트를 작성합니다.', icon: '🔍' },
    ]

    if (!contractData?.routes?.length) return defaultActions

    const base = contractData.routes
      .map((route) => routeByApiPath[route.path])
      .filter(Boolean) as QuickAction[]

    return base.length ? base : defaultActions
  }, [contractData])

  const [health, setHealth] = useState<'ok' | 'bad' | 'loading'>('loading')

  useEffect(() => {
    healthApi
      .ping()
      .then(() => setHealth('ok'))
      .catch(() => setHealth('bad'))
  }, [])

  return (
    <section style={{ animation: 'fadeIn 0.5s ease-out' }}>
      <header className="home-hero">
        <p className="home-eyebrow">Smart Content Orchestrator</p>
        <h1>Cardra</h1>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '18px', maxWidth: '320px', margin: '0 auto var(--space-lg)' }}>
          당신의 아이디어를 임팩트 있는 <strong style={{ color: 'var(--color-main)' }}>카드 뉴스</strong>로 변환하세요.
        </p>
        <HealthBadge health={health} />
      </header>

      <div className="home-actions" style={{ display: 'grid', gap: 'var(--space-md)' }}>
        {quickActions.map((action) => (
          <Link key={action.key} to={action.to} className="home-link">
            <button className="card" style={{ 
              width: '100%', 
              textAlign: 'left', 
              display: 'flex', 
              alignItems: 'center', 
              gap: 'var(--space-md)',
              padding: 'var(--space-lg)',
              border: '2px solid transparent',
            }}>
              <span style={{ fontSize: '32px' }}>{action.icon}</span>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <span style={{ fontSize: '18px', fontWeight: 800 }}>{action.label}</span>
                <span className="muted">{action.detail}</span>
              </div>
              <span style={{ marginLeft: 'auto', color: 'var(--color-main)', fontWeight: 800 }}>→</span>
            </button>
          </Link>
        ))}
      </div>

      <footer style={{ marginTop: 'var(--space-xl)', textAlign: 'center' }}>
        <p className="muted" style={{ fontSize: '12px' }}>
          Powered by Gemini Agent Engine
        </p>
      </footer>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .home-link .card:hover {
          border-color: var(--color-main);
          background: var(--color-main-soft);
          transform: translateY(-2px);
        }
      `}</style>
    </section>
  )
}
