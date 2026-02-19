import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { researchApi } from '../lib/api'

export function ResearchResultPage() {
  const { jobId } = useParams()
  const navigate = useNavigate()
  const { data, isLoading, error } = useQuery({
    queryKey: ['research-result', jobId],
    enabled: Boolean(jobId),
    queryFn: () => researchApi.getResult(jobId ?? ''),
  })

  if (isLoading) return <div>결과 조회 중...</div>

  if (error) {
    return <div className="error">결과 조회 실패</div>
  }

  if (!data) return null

  return (
    <div>
      <h2>연구 결과</h2>
      <p>상태: {data.status}</p>
      {data.cache?.hit && <p>캐시 히트 + TTL: {data.cache.ttlSec}s</p>}
      <pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(data.result, null, 2)}</pre>
      <button className="primary" onClick={() => navigate('/')}>홈으로</button>
    </div>
  )
}
