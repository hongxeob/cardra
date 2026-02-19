import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { cardApi } from '../lib/api'
import { toAppError } from '../lib/error'
import { CardRenderer } from '../features/card/CardRenderer'
import { ErrorCard } from '../components/ErrorCard'
import { LoadingCard } from '../components/LoadingCard'

export function CardDetailPage() {
  const { id } = useParams()

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['card', id],
    queryFn: () => cardApi.getCard(id ?? ''),
    enabled: Boolean(id),
  })

  if (isLoading) {
    return <LoadingCard label="카드를 불러오는 중입니다." />
  }

  if (error) {
    const err = toAppError(error)
    return <ErrorCard error={err} onRetry={() => refetch()} />
  }

  if (!data) return null

  return <CardRenderer card={data} />
}
