import { Navigate, Route, Routes, Link, useLocation } from 'react-router-dom'
import { HomePage } from './pages/HomePage'
import { CreatePage } from './pages/CreatePage'
import { CardDetailPage } from './pages/CardDetailPage'
import { ResearchLoadingPage } from './pages/ResearchLoadingPage'
import { ResearchResultPage } from './pages/ResearchResultPage'
import { ErrorPage } from './pages/ErrorPage'
import { NotFoundPage } from './pages/NotFoundPage'

function Header() {
  const location = useLocation()
  const isHome = location.pathname === '/'

  return (
    <header className="global-header" style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: 'var(--space-md) var(--space-md)',
      background: 'rgba(255, 255, 255, 0.8)',
      backdropFilter: 'blur(10px)',
      position: 'sticky',
      top: 0,
      zIndex: 100,
      borderBottom: '1px solid var(--color-border)',
      marginBottom: 'var(--space-md)'
    }}>
      <Link to="/" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '8px' }}>
        <span style={{ 
          background: 'var(--color-main)', 
          color: '#fff', 
          width: '32px', 
          height: '32px', 
          borderRadius: '8px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 900,
          fontSize: '20px'
        }}>C</span>
        <span style={{ fontWeight: 900, fontSize: '20px', color: 'var(--color-text)', letterSpacing: '-0.02em' }}>Cardra</span>
      </Link>
      
      {!isHome && (
        <Link to="/create" className="home-link">
          <button className="primary" style={{ minHeight: '36px', padding: '0 12px', fontSize: '14px' }}>
            + 생성
          </button>
        </Link>
      )}
    </header>
  )
}

export function App() {
  return (
    <div className="app-shell" style={{ padding: 0 }}>
      <Header />
      <main style={{ padding: '0 var(--space-md) 80px' }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/create" element={<CreatePage />} />
          <Route path="/cards/:id" element={<CardDetailPage />} />
          <Route path="/research/loading/:jobId" element={<ResearchLoadingPage />} />
          <Route path="/research/:jobId/result" element={<ResearchResultPage />} />
          <Route path="/error" element={<ErrorPage />} />
          <Route path="*" element={<NotFoundPage />} />
          <Route path="/home" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  )
}
