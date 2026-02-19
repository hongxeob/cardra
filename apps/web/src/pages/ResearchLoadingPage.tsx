import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { researchApi } from '../lib/api'
import { toAppError } from '../lib/error'

export function ResearchLoadingPage() {
  const { jobId } = useParams()
  const navigate = useNavigate()

  const { data, dataUpdatedAt, error } = useQuery({
    queryKey: ['research-status', jobId],
    enabled: Boolean(jobId),
    queryFn: () => researchApi.getStatus(jobId ?? ''),
    refetchInterval: 2500,
  })

  const cancelMut = useMutation({
    mutationFn: () => researchApi.cancel(jobId ?? ''),
  })

  useEffect(() => {
    if (data?.status && ['completed', 'failed', 'cancelled'].includes(data.status)) {
      navigate(`/research/${jobId}/result`)
    }
  }, [data?.status, jobId, navigate])

  if (error) {
    const err = toAppError(error)
    return <div className="error">{err.message}</div>
  }

  return (
    <div>
      <h2>리서치 진행중</h2>
      <p>jobId: {jobId}</p>
      <p>마지막 갱신: {new Date(dataUpdatedAt).toLocaleTimeString()}</p>
      <p>상태: {data?.status ?? 'loading'}</p>
      <p>createdAt: {data?.createdAt}</p>
      <div className="row" style={{ marginTop: 12 }}>
        <button className="secondary" onClick={() => navigate(`/research/${jobId}/result`)}>
          결과 확인
        </button>
        <button
          className="primary"
          onClick={() => cancelMut.mutate()}
          disabled={cancelMut.isPending}
        >
          취소
        </button>
      </div>
    </div>
  )
}
