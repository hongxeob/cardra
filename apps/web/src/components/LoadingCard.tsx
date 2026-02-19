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
      <p style={{ fontWeight: 600, color: 'var(--color-text-muted)' }}>{label ?? '데이터를 처리하고 있습니다...'}</p>
    </section>
  )
}
