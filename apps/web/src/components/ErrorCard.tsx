import { useEffect, useMemo, useState } from 'react'
import { AppError } from '../lib/error'

function useRetryCountDown(retryAfter?: number) {
  const [remain, setRemain] = useState<number | null>(retryAfter ?? null)

  useEffect(() => {
    if (retryAfter === undefined || retryAfter === null || retryAfter <= 0) {
      setRemain(null)
      return
    }

    setRemain(retryAfter)

    const timer = window.setInterval(() => {
      setRemain((current) => {
        if (current === null || current <= 1) {
          window.clearInterval(timer)
          return null
        }
        return current - 1
      })
    }, 1000)

    return () => {
      window.clearInterval(timer)
    }
  }, [retryAfter])

  return remain
}

export function ErrorCard({ error, onRetry, onBack }: { error: AppError; onRetry?: () => void; onBack?: () => void }) {
  const remain = useRetryCountDown(error.retryAfter)
  const disabled = remain !== null
  const retryLabel = useMemo(() => (remain !== null ? `다시 시도 (${remain}s)` : '다시 시도'), [remain])

  return (
    <section className="card error-card" style={{ borderTop: '4px solid var(--color-error)' }}>
      <h3 className="error" style={{ marginBottom: 'var(--space-sm)' }}>요청 처리 실패</h3>
      <p style={{ fontSize: '16px', marginBottom: 'var(--space-md)' }}>{error.message}</p>
      
      <div style={{ display: 'grid', gap: 'var(--space-sm)', padding: 'var(--space-md)', background: 'var(--color-bg)', borderRadius: 'var(--radius-sm)', marginBottom: 'var(--space-lg)' }}>
        <p className="muted" style={{ fontSize: '12px' }}>Error Code: {error.code}</p>
        {error.traceId && <p className="muted" style={{ fontSize: '11px' }}>Trace: {error.traceId}</p>}
      </div>

      <div className="row" style={{ gap: 'var(--space-md)' }}>
        {error.retryable && onRetry && (
          <button className="primary" style={{ flex: 1 }} onClick={onRetry} disabled={disabled}>
            {retryLabel}
          </button>
        )}
        {onBack && (
          <button className="secondary" style={{ flex: 1 }} onClick={onBack}>
            홈으로 돌아가기
          </button>
        )}
      </div>
      
      {error.retryAfter && (
        <p className="muted" style={{ textAlign: 'center', marginTop: 'var(--space-sm)' }}>
          {remain !== null ? '안정화 대기 중입니다.' : '이제 다시 시도 가능합니다.'}
        </p>
      )}
    </section>
  )
}
