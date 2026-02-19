import { Navigate, Route, Routes } from 'react-router-dom'
import { HomePage } from './pages/HomePage'
import { CreatePage } from './pages/CreatePage'
import { CardDetailPage } from './pages/CardDetailPage'
import { ResearchLoadingPage } from './pages/ResearchLoadingPage'
import { ResearchResultPage } from './pages/ResearchResultPage'
import { ErrorPage } from './pages/ErrorPage'
import { NotFoundPage } from './pages/NotFoundPage'

export function App() {
  return (
    <div className="app-shell">
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
    </div>
  )
}
