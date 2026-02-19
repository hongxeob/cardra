import { AppError } from '../lib/error'

export function ErrorCard({ error, onRetry, onBack }: { error: AppError; onRetry?: () => void; onBack?: () => void }) {
  return (
    <section className="card">
      <h3>요청 처리 실패</h3>
      <p className="error">{error.message}</p>
      <p className="muted">코드: {error.code}</p>
      {error.retryable && onRetry ? (
        <button className="primary" onClick={onRetry}>
          다시 시도
        </button>
      ) : null}
      {onBack ? (
        <button className="secondary" onClick={onBack}>
          뒤로 가기
        </button>
      ) : null}
      {error.traceId ? <p className="muted">Trace: {error.traceId}</p> : null}
    </section>
  )
}
