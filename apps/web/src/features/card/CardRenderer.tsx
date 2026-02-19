import type { CardResponse } from '../../lib/types'
import { CardItemBlock } from './CardItemBlock'
import { useNavigate } from 'react-router-dom'

export function CardRenderer({ card }: { card: CardResponse }) {
  const navigate = useNavigate()

  return (
    <div style={{ animation: 'fadeIn 0.6s ease-out' }}>
      <header style={{ marginBottom: 'var(--space-xl)', textAlign: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 'var(--space-sm)', marginBottom: 'var(--space-sm)' }}>
          <span className="badge" style={{ 
            background: 'var(--color-main)', 
            color: '#fff', 
            border: 'none',
            padding: '4px 12px',
            fontSize: '11px',
            fontWeight: 800
          }}>{card.status}</span>
          <span className="muted" style={{ fontSize: '11px' }}>{new Date(card.createdAt).toLocaleDateString()}</span>
        </div>
        <h2 style={{ fontSize: '36px', letterSpacing: '-0.04em', fontWeight: 900, marginBottom: 'var(--space-md)' }}>{card.keyword}</h2>
        
        <div className="row" style={{ justifyContent: 'center', gap: 'var(--space-md)' }}>
           <button className="secondary" style={{ minHeight: '36px', fontSize: '14px', borderRadius: 'var(--radius-full)' }}>
             📂 저장하기
           </button>
           <button className="secondary" style={{ minHeight: '36px', fontSize: '14px', borderRadius: 'var(--radius-full)' }}>
             🔗 공유하기
           </button>
        </div>
      </header>

      <div style={{ display: 'grid', gap: 'var(--space-lg)' }}>
        {card.cards.map((item, index) => (
          <div key={`${card.id}-${index}`} style={{ animation: `slideUp 0.5s ease-out ${index * 0.1}s both` }}>
            <CardItemBlock item={item} />
          </div>
        ))}
      </div>

      <footer style={{ marginTop: 'var(--space-xl)', textAlign: 'center', display: 'grid', gap: 'var(--space-md)' }}>
        <button className="primary" style={{ width: '100%' }} onClick={() => navigate('/create')}>
          새로운 카드 만들기
        </button>
        <button className="secondary" style={{ width: '100%', background: 'none', border: '1px solid var(--color-border)' }} onClick={() => navigate('/')}>
          목록으로 이동
        </button>
      </footer>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
        @keyframes slideUp {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  )
}
