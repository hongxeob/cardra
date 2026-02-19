import { useLocation, Link } from 'react-router-dom'

export function ErrorPage() {
  const { state } = useLocation() as { state: { message?: string } | null }

  return (
    <div>
      <h2>오류</h2>
      <p className="error">{state?.message ?? '알 수 없는 오류가 발생했습니다.'}</p>
      <Link to="/">홈으로 이동</Link>
    </div>
  )
}
