import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { ErrorBox } from './components/ui'
import { useMe } from './lib/auth'
import { AuthPage } from './pages/AuthPage'
import { HistoryPage } from './pages/HistoryPage'
import { PortfolioPage } from './pages/PortfolioPage'
import { StockPage } from './pages/StockPage'
import { TradePage } from './pages/TradePage'

function App() {
  const me = useMe()

  if (me.isPending) return <p className="p-8 text-slate-500">Loading…</p>
  if (me.isError)
    return (
      <div className="p-8">
        <ErrorBox error={new Error('Cannot reach the backend. Is it running?')} />
      </div>
    )
  if (!me.data) return <AuthPage />

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<PortfolioPage />} />
          <Route path="trade" element={<TradePage />} />
          <Route path="stocks/:symbol" element={<StockPage />} />
          <Route path="history" element={<HistoryPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
