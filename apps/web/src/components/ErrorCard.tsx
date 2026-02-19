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
    <section className="card error-card">
      <h3>요청 처리 실패</h3>
      <p className="error">{error.message}</p>
      <p className="muted">코드: {error.code}</p>
      {error.retryable && onRetry ? (
        <button className="primary" onClick={onRetry} disabled={disabled}>
          {retryLabel}
        </button>
      ) : null}
      {onBack ? (
        <button className="secondary" onClick={onBack}>
          뒤로 가기
        </button>
      ) : null}
      {error.traceId ? <p className="muted">Trace: {error.traceId}</p> : null}
      {error.retryAfter ? (
        <p className="muted">{remain !== null ? '안정화 대기 중입니다.' : '이제 다시 시도 가능합니다.'}</p>
      ) : null}
    </section>
  )
}
