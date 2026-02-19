export function LoadingCard({ label }: { label?: string }) {
  return (
    <section className="card loading-card" style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      alignItems: 'center', 
      justifyContent: 'center', 
      gap: 'var(--space-lg)',
      minHeight: '200px',
      textAlign: 'center'
    }}>
      <div className="spinner" style={{ width: '40px', height: '40px', borderWidth: '4px' }}></div>
      <p style={{ fontWeight: 600, color: 'var(--color-text-muted)' }}>{label ?? '최신 뉴스 및 트렌드 데이터를 수집 중입니다...'}</p>
    </section>
  )
}
