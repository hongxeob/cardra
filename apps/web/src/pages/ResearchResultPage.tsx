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
    return <LoadingCard label="딥 리서치 결과를 분석 중입니다..." />
  }

  if (error) {
    const err = toAppError(error)
    return <ErrorCard error={err} onRetry={() => refetch()} onBack={() => navigate('/')} />
  }

  if (!data) {
    return null
  }

  return (
    <div style={{ animation: 'fadeIn 0.6s ease-out' }}>
      <header style={{ marginBottom: 'var(--space-xl)' }}>
        <button className="muted" onClick={() => navigate('/create')} style={{ background: 'none', padding: 0, minHeight: 'auto', marginBottom: 'var(--space-md)' }}>
          ← 다시 생성하기
        </button>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-sm)' }}>
          <span className="badge" style={{ background: 'var(--color-main)', color: '#fff', border: 'none' }}>RESEARCH REPORT</span>
          <span className="muted" style={{ fontSize: '12px' }}>Job ID: {jobId}</span>
        </div>
        <h2 style={{ fontSize: '32px', marginTop: 'var(--space-sm)', fontWeight: 900, letterSpacing: '-0.04em' }}>리서치 분석 리포트</h2>
      </header>

      {data.result ? (
        <ResearchResultView result={data.result} />
      ) : (
        <div className="card" style={{ textAlign: 'center', padding: 'var(--space-xl)' }}>
          <p className="muted">아직 분석 결과 데이터가 준비되지 않았습니다.</p>
        </div>
      )}

      {data.cache?.hit && (
        <p className="muted" style={{ marginTop: 'var(--space-md)', textAlign: 'center', fontSize: '11px' }}>
          * 이 리포트는 최근 분석된 최신 데이터를 기반으로 캐싱되었습니다. (TTL: {data.cache.ttlSec}s)
        </p>
      )}

      <footer style={{ marginTop: 'var(--space-xl)', textAlign: 'center', display: 'grid', gap: 'var(--space-md)' }}>
        <button className="primary" style={{ width: '100%' }} onClick={() => navigate('/')}>
          홈으로 돌아가기
        </button>
      </footer>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  )
}
