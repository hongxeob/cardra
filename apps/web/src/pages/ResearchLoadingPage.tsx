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
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return (status === 'completed' || status === 'failed' || status === 'cancelled') ? false : 2000
    },
  })

  const cancelMut = useMutation({
    mutationFn: () => researchApi.cancel(jobId ?? ''),
  })

  useEffect(() => {
    if (data?.status === 'completed') {
      const timer = setTimeout(() => {
        navigate(`/research/${jobId}/result`)
      }, 1500)
      return () => clearTimeout(timer)
    }
    if (data?.status === 'failed' || data?.status === 'cancelled') {
      navigate(`/research/${jobId}/result`)
    }
  }, [data?.status, jobId, navigate])

  if (isLoading) {
    return <LoadingCard label="서버와 연결을 확인 중입니다..." />
  }

  if (error) {
    const err = toAppError(error)
    return (
      <ErrorCard
        error={err}
        onRetry={() => refetch()}
        onBack={() => navigate('/create')}
      />
    )
  }

  const isCompleted = data?.status === 'completed'
  const isFailed = data?.status === 'failed'

  return (
    <div style={{ animation: 'fadeIn 0.5s ease-out', padding: 'var(--space-xl) 0' }}>
      <header style={{ textAlign: 'center', marginBottom: 'var(--space-xl)' }}>
        <div className="spinner" style={{ 
          width: '64px', 
          height: '64px', 
          margin: '0 auto var(--space-lg)',
          borderWidth: '6px',
          borderTopColor: isCompleted ? 'var(--color-success)' : isFailed ? 'var(--color-error)' : 'var(--color-main)'
        }}></div>
        <h2 style={{ fontSize: '28px' }}>
          {isCompleted ? '분석이 완료되었습니다!' : isFailed ? '분석 중 오류 발생' : '데이터를 딥 리서치 중입니다'}
        </h2>
        <p className="muted" style={{ marginTop: 'var(--space-sm)' }}>
          {isCompleted ? '결과 리포트를 생성하고 있습니다.' : `Job ID: ${jobId}`}
        </p>
      </header>

      <div className="card" style={{ background: 'var(--color-surface)', display: 'grid', gap: 'var(--space-md)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontWeight: 600 }}>상태</span>
          <span className="badge" style={{ 
            background: isCompleted ? 'var(--color-success)' : isFailed ? 'var(--color-error)' : 'var(--color-main-soft)',
            color: isCompleted || isFailed ? '#fff' : 'var(--color-main)',
            border: 'none',
            textTransform: 'uppercase'
          }}>
            {data?.status ?? 'pending'}
          </span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontWeight: 600 }}>시작 시간</span>
          <span className="muted">{data?.createdAt ? new Date(data.createdAt).toLocaleTimeString() : '-'}</span>
        </div>
      </div>

      <div className="row" style={{ marginTop: 'var(--space-xl)', gap: 'var(--space-md)' }}>
        <button 
          className="secondary" 
          style={{ flex: 1 }} 
          onClick={() => navigate(`/research/${jobId}/result`)}
          disabled={!isCompleted && !isFailed}
        >
          결과 미리보기
        </button>
        <button 
          className="primary" 
          style={{ flex: 1 }} 
          onClick={() => cancelMut.mutate()} 
          disabled={cancelMut.isPending || isCompleted || isFailed || data?.status === 'cancelled'}
        >
          {cancelMut.isPending ? '취소 중...' : '작업 취소'}
        </button>
      </div>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  )
}
