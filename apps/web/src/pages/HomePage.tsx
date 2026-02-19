import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { healthApi } from '../lib/api'

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
    <div>
      <h1>Cardra</h1>
      <p>키워드 기반 카드뉴스를 빠르게 생성해보세요.</p>
      <p>시스템 상태: {health}</p>
      <div className="row">
        <button className="primary" onClick={() => navigate('/create')}>
          카드 생성하기
        </button>
        <Link to="/create">
          <button className="secondary">리서치 시작</button>
        </Link>
      </div>
    </div>
  )
}
