import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { researchApi } from '../lib/api'
import { toAppError } from '../lib/error'
import { ErrorCard } from '../components/ErrorCard'
import { LoadingCard } from '../components/LoadingCard'

export function ResearchLoadingPage() {
  const { jobId } = useParams()
  const navigate = useNavigate()

  const { data, error, isLoading, refetch } = useQuery({
    queryKey: ['research-status', jobId],
    enabled: Boolean(jobId),
    queryFn: () => researchApi.getStatus(jobId ?? ''),
    refetchInterval: 2500,
  })

  const cancelMut = useMutation({
    mutationFn: () => researchApi.cancel(jobId ?? ''),
  })

  useEffect(() => {
    if (data?.status === 'completed' || data?.status === 'cancelled') {
      navigate(`/research/${jobId}/result`)
    }
  }, [data?.status, jobId, navigate])

  if (isLoading) {
    return <LoadingCard label="리서치 상태를 확인 중입니다." />
  }

  if (error) {
    const err = toAppError(error)
    return (
      <ErrorCard
        error={err}
        onRetry={() => refetch()}
        onBack={() => {
          if (jobId) {
            navigate('/create')
          }
        }}
      />
    )
  }

  if (!data) return null

  const isFailed = data.status === 'failed'

  return (
    <div className="card">
      <h2>리서치 진행중</h2>
      <p>jobId: {jobId}</p>
      <p>상태: {data.status}</p>
      {data.createdAt ? <p>createdAt: {data.createdAt}</p> : null}
      {data.updatedAt ? <p>마지막 갱신: {data.updatedAt}</p> : null}

      {isFailed && data.error ? (
        <div className="muted">
          <p>실패: {data.error.message}</p>
          <p>코드: {data.error.code}</p>
        </div>
      ) : null}

      <div className="row" style={{ marginTop: 12 }}>
        <button className="secondary" onClick={() => navigate('/research/' + jobId + '/result')}>
          결과 확인
        </button>

        <button
          className="primary"
          onClick={() => cancelMut.mutate()}
          disabled={cancelMut.isPending || data.status === 'cancelled' || data.status === 'completed' || isFailed}
        >
          {cancelMut.isPending ? '취소중...' : '취소'}
        </button>
      </div>
    </div>
  )
}
