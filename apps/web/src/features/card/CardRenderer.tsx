import type { CardResponse } from '../../lib/types'
import { CardItemBlock } from './CardItemBlock'
import { useNavigate } from 'react-router-dom'

export function CardRenderer({ card }: { card: CardResponse }) {
  const navigate = useNavigate()

  return (
    <div style={{ animation: 'fadeIn 0.6s ease-out' }}>
      <header style={{ marginBottom: 'var(--space-xl)' }}>
        <button className="muted" onClick={() => navigate('/create')} style={{ background: 'none', padding: 0, minHeight: 'auto', marginBottom: 'var(--space-md)' }}>
          ← 다시 만들기
        </button>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-sm)' }}>
          <span className="badge" style={{ background: 'var(--color-main)', color: '#fff', border: 'none' }}>{card.status}</span>
          <span className="muted" style={{ fontSize: '12px' }}>{new Date(card.createdAt).toLocaleString()}</span>
        </div>
        <h2 style={{ fontSize: '32px', marginTop: 'var(--space-sm)' }}>{card.keyword}</h2>
      </header>

      <div style={{ display: 'grid', gap: 'var(--space-lg)' }}>
        {card.cards.map((item, index) => (
          <CardItemBlock key={`${card.id}-${index}`} item={item} />
        ))}
      </div>

      <footer style={{ marginTop: 'var(--space-xl)', textAlign: 'center' }}>
        <button className="secondary" style={{ width: '100%' }} onClick={() => navigate('/')}>
          홈으로 돌아가기
        </button>
      </footer>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  )
}
