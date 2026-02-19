export function LoadingCard({ label }: { label?: string }) {
  return (
    <section className="card loading-card">
      <p>{label ?? '불러오는 중입니다.'}</p>
      <div className="spinner" aria-label="loading"></div>
    </section>
  )
}
