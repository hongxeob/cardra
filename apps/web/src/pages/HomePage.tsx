import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { healthApi, uiApi } from '../lib/api'
import { storage } from '../lib/storage'

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

  const recentCards = useMemo(() => storage.getRecentCards(), [])

  return (
    <section style={{ animation: 'fadeIn 0.5s ease-out' }}>
      <header className="home-hero" style={{ paddingTop: 'var(--space-md)' }}>
        <p className="home-eyebrow">Real-time Hot Issue Summary</p>
        <h1 style={{ letterSpacing: '-0.04em', fontWeight: 900 }}>Cardra</h1>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '18px', maxWidth: '360px', margin: '0 auto var(--space-lg)', lineHeight: 1.4 }}>
          키워드 하나로 지금 가장 <strong style={{ color: 'var(--color-main)' }}>뜨거운 이슈</strong>를 2~3장 카드뉴스로 요약하세요.
        </p>
        <HealthBadge health={health} />
      </header>

      <div className="home-actions" style={{ display: 'grid', gap: 'var(--space-md)', marginBottom: 'var(--space-xl)' }}>
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
              background: action.key.includes('create') ? 'var(--color-surface)' : 'var(--color-bg)'
            }}>
              <span style={{ fontSize: '32px' }}>{action.icon}</span>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <span style={{ fontSize: '18px', fontWeight: 800 }}>{action.label}</span>
                <span className="muted">{action.key.includes('create') ? '지금 유행하는 키워드로 요약 생성' : '데이터 기반 딥 분석 리포트'}</span>
              </div>
              <span style={{ marginLeft: 'auto', color: 'var(--color-main)', fontWeight: 800 }}>→</span>
            </button>
          </Link>
        ))}
      </div>

      <section className="recent-section">
        <h3 style={{ fontSize: '14px', color: 'var(--color-text-muted)', marginBottom: 'var(--space-md)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>최근 생성된 카드</h3>
        {recentCards.length > 0 ? (
          <div style={{ display: 'grid', gap: 'var(--space-sm)' }}>
            {recentCards.map(card => (
              <Link key={card.id} to={`/cards/${card.id}`} style={{ textDecoration: 'none' }}>
                <div className="card" style={{ padding: 'var(--space-md)', display: 'flex', alignItems: 'center', gap: 'var(--space-md)', border: '1px solid var(--color-border)' }}>
                  <span style={{ fontSize: '24px' }}>📄</span>
                  <div style={{ flex: 1 }}>
                    <p style={{ fontWeight: 700, fontSize: '15px', color: 'var(--color-text)' }}>{card.keyword}</p>
                    <p className="muted" style={{ fontSize: '12px' }}>{new Date(card.createdAt).toLocaleDateString()}</p>
                  </div>
                  <span style={{ color: 'var(--color-border)' }}>›</span>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="card" style={{ padding: 'var(--space-xl)', textAlign: 'center', background: 'var(--color-bg)', border: '1px dashed var(--color-border)' }}>
            <p className="muted">아직 생성된 카드가 없습니다.</p>
            <Link to="/create" style={{ display: 'inline-block', marginTop: 'var(--space-sm)', color: 'var(--color-main)', fontWeight: 700 }}>첫 카드 만들기 →</Link>
          </div>
        )}
      </section>

      <footer style={{ marginTop: 'var(--space-xl)', textAlign: 'center', padding: 'var(--space-lg) 0' }}>
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
          background: var(--color-main-soft) !important;
          transform: translateY(-2px);
        }
        .recent-section .card:hover {
          border-color: var(--color-sub);
          background: var(--color-bg);
        }
      `}</style>
    </section>
  )
}
