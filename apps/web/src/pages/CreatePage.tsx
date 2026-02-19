import { FormEvent, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { cardApi, researchApi } from '../lib/api'
import { toAppError } from '../lib/error'

export function CreatePage() {
  const [keyword, setKeyword] = useState('')
  const [tone, setTone] = useState('neutral')
  const navigate = useNavigate()

  const cardMut = useMutation({
    mutationFn: () => cardApi.generate({ keyword, tone }),
    onSuccess: (res) => navigate(`/cards/${res.id}`),
    onError: () => navigate('/error', { state: { message: '카드 생성 실패' } }),
  })

  const [researchMode, setResearchMode] = useState(false)
  const researchMut = useMutation({
    mutationFn: () =>
      researchApi.createJob({
        keyword,
        timeRange: '24h',
      }),
    onSuccess: (res) => {
      if (researchMode) {
        navigate(`/research/loading/${res.jobId}`)
      }
    },
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!keyword.trim()) return
    cardMut.mutate()
    if (researchMode) {
      researchMut.mutate()
    }
  }

  const errorText = cardMut.error ? toAppError(cardMut.error as unknown).message : null

  return (
    <div>
      <h2>카드 생성</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>키워드</label>
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        </div>
        <div>
          <label>톤</label>
          <input value={tone} onChange={(e) => setTone(e.target.value)} />
        </div>
        <label style={{ display: 'block', marginTop: 8 }}>
          <input
            type="checkbox"
            checked={researchMode}
            onChange={(e) => setResearchMode(e.target.checked)}
          />
          리서치 결과도 함께 실행
        </label>
        <div className="row" style={{ marginTop: 12 }}>
          <button className="primary" type="submit" disabled={cardMut.isPending}>
            {cardMut.isPending ? '생성중...' : '생성'}
          </button>
          {errorText && <span className="error">{errorText}</span>}
        </div>
      </form>
    </div>
  )
}
