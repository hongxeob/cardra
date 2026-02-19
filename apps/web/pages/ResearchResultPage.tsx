import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { researchApi } from '../lib/api'
import { toAppError } from '../lib/error'
import { ErrorCard } from '../components/ErrorCard'
import { LoadingCard } from '../components/LoadingCard'
import { ResearchResultView } from '../features/research/ResearchResultView'

export function ResearchResultPage() {
  const { jobId } = useParams()
  const navigate = useNavigate()

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['research-result', jobId],
    enabled: Boolean(jobId),
    queryFn: () => researchApi.getResult(jobId ?? ''),
  })

  if (isLoading) {
    return <LoadingCard label="결과 조회 중..." />
  }

  if (error) {
    const err = toAppError(error)
    return <ErrorCard error={err} onRetry={() => refetch()} onBack={() => navigate('/')} />
  }

  if (!data) {
    return null
  }

  return (
    <div className="research-result-page">
      <h2>연구 결과</h2>
      <p>상태: {data.status}</p>

      {data.cache?.hit ? <p className="muted">캐시 히트 + TTL: {data.cache.ttlSec}s</p> : null}

      {data.result ? <ResearchResultView result={data.result} /> : <p>아직 결과가 비어있습니다.</p>}

      {!data.result && data.error ? (
        <div className="card">
          <p className="error">{data.error.message}</p>
          <p className="muted">코드: {data.error.code}</p>
        </div>
      ) : null}

      <div className="row" style={{ marginTop: 16 }}>
        <button className="primary" onClick={() => navigate('/')}>홈으로</button>
        <button className="secondary" onClick={() => navigate('/create')}>다시 생성</button>
      </div>
    </div>
  )
}
