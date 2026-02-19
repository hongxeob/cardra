import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { healthApi } from '../lib/api'

function HealthBadge({ health }: { health: 'ok' | 'bad' | 'loading' }) {
  const label = health === 'ok' ? '정상' : health === 'bad' ? '점검 필요' : '체크중'
  const tone = health === 'ok' ? 'primary' : health === 'bad' ? 'error' : 'muted'

  return <span className={`health-badge health-${tone}`}>{`시스템 ${label}`}</span>
}

export function HomePage() {
  const navigate = useNavigate()
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
        <button className="primary" onClick={() => navigate('/create')}>
          새 카드 만들기
        </button>
        <button className="secondary" onClick={() => navigate('/create')}>
          리서치 시작
        </button>
      </div>
    </section>
  )
}
