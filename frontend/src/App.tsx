import { useQuery } from '@tanstack/react-query'

type Health = { status: string }

async function fetchHealth(): Promise<Health> {
  const res = await fetch('/actuator/health')
  // Actuator returns 503 with a JSON body when DOWN, so read the body either way.
  return (await res.json()) as Health
}

function App() {
  const health = useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth,
    refetchInterval: 10_000,
    retry: false,
  })

  const status = health.isPending ? 'checking…' : health.isError ? 'unreachable' : health.data.status
  const color =
    status === 'UP' ? 'bg-emerald-500' : health.isPending ? 'bg-slate-400' : 'bg-rose-500'

  return (
    <main className="mx-auto max-w-xl px-4 py-16">
      <h1 className="text-3xl font-semibold tracking-tight">Stock Simulator</h1>
      <p className="mt-2 text-slate-600">Paper trading with real market prices.</p>

      <div className="mt-8 flex items-center gap-3 rounded-lg border border-slate-200 p-4">
        <span className={`h-3 w-3 rounded-full ${color}`} aria-hidden="true" />
        <span>
          Backend: <strong>{status}</strong>
        </span>
      </div>
    </main>
  )
}

export default App
