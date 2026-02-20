import { useState } from 'react'
import type { CardResponse } from '../../lib/types'
import { CardItemBlock } from './CardItemBlock'
import { useNavigate } from 'react-router-dom'
import { storage } from '../../lib/storage'

export function CardRenderer({ card }: { card: CardResponse }) {
  const navigate = useNavigate()
  const [saveState, setSaveState] = useState<'idle' | 'saved' | 'error'>('idle')
  const [shareState, setShareState] = useState<'idle' | 'shared' | 'copied' | 'error'>('idle')

  const showSaveFeedback = (nextState: 'saved' | 'error') => {
    setSaveState(nextState)
    setTimeout(() => setSaveState('idle'), 2000)
  }

  const showShareFeedback = (nextState: 'shared' | 'copied' | 'error') => {
    setShareState(nextState)
    setTimeout(() => setShareState('idle'), 2000)
  }

  const copyToClipboard = async (text: string) => {
    if (navigator.clipboard?.writeText) {
      try {
        await navigator.clipboard.writeText(text)
        return true
      } catch {
        // Fallback to legacy copy flow below when Clipboard API is blocked.
      }
    }

    try {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.setAttribute('readonly', 'true')
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.focus()
      textarea.select()
      const copied = document.execCommand('copy')
      document.body.removeChild(textarea)
      return copied
    } catch {
      return false
    }
  }

  const handleSave = () => {
    try {
      storage.saveCard(card)
      showSaveFeedback('saved')
    } catch {
      showSaveFeedback('error')
    }
  }

  const handleShare = async () => {
    const shareUrl = window.location.href

    if (navigator.share) {
      try {
        await navigator.share({
          title: `${card.keyword} | Cardra`,
          text: `${card.keyword} 카드`,
          url: shareUrl,
        })
        showShareFeedback('shared')
        return
      } catch (error) {
        if (error instanceof Error && error.name === 'AbortError') {
          return
        }
      }
    }

    const copied = await copyToClipboard(shareUrl)

    if (copied) {
      showShareFeedback('copied')
      return
    }

    window.prompt('공유 링크를 복사해 주세요.', shareUrl)
    showShareFeedback('error')
  }

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
           <button
             className="secondary"
             style={{ minHeight: '36px', fontSize: '14px', borderRadius: 'var(--radius-full)' }}
             onClick={handleSave}
           >
             {saveState === 'saved' ? '✅ 저장됨!' : saveState === 'error' ? '⚠️ 저장 실패' : '📂 저장하기'}
           </button>
           <button
             className="secondary"
             style={{ minHeight: '36px', fontSize: '14px', borderRadius: 'var(--radius-full)' }}
             onClick={handleShare}
           >
             {shareState === 'shared' ? '✅ 공유됨!' : shareState === 'copied' ? '🔗 복사됨!' : shareState === 'error' ? '⚠️ 링크 확인' : '🔗 공유하기'}
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
