import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { researchApi } from '../lib/api'
import { toAppError } from '../lib/error'
import { ErrorCard } from '../components/ErrorCard'
import { LoadingCard } from '../components/LoadingCard'

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
    <div>
      <h2>연구 결과</h2>
      <p>상태: {data.status}</p>
      {data.cache?.hit ? <p>캐시 히트 + TTL: {data.cache.ttlSec}s</p> : null}
      {data.result ? (
        <pre className="result-block">{JSON.stringify(data.result, null, 2)}</pre>
      ) : (
        <p>아직 결과가 비어있습니다.</p>
      )}
      <button className="primary" onClick={() => navigate('/')}>홈으로</button>
    </div>
  )
}
