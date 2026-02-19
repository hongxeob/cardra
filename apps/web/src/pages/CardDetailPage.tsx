import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { cardApi } from '../lib/api'
import { CardRenderer } from '../features/card/CardRenderer'
import { toAppError } from '../lib/error'

export function CardDetailPage() {
  const { id } = useParams()

  const { data, isLoading, error } = useQuery({
    queryKey: ['card', id],
    queryFn: () => cardApi.getCard(id ?? ''),
    enabled: Boolean(id),
  })

  if (isLoading) {
    return <div>불러오는 중...</div>
  }

  if (error) {
    const err = toAppError(error)
    return <div className="error">{err.message}</div>
  }

  if (!data) return <div>카드가 없습니다.</div>

  return <CardRenderer card={data} />
}
