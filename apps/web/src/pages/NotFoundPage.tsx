import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div>
      <h2>404</h2>
      <p>요청한 페이지를 찾을 수 없습니다.</p>
      <Link to="/">홈으로</Link>
    </div>
  )
}
