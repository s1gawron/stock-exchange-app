import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { api } from '../lib/api'
import type { Me } from '../lib/types'
import { ThemeToggle } from '../components/ThemeToggle'
import { buttonClass, Card, ErrorBox, inputClass } from '../components/ui'

export function AuthPage() {
  const qc = useQueryClient()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [password, setPassword] = useState('')

  const submit = useMutation({
    mutationFn: () =>
      mode === 'login'
        ? api<Me>('/api/auth/login', { method: 'POST', body: { email, password } })
        : api<Me>('/api/auth/register', { method: 'POST', body: { email, displayName, password } }),
    onSuccess: (me) => qc.setQueryData(['me'], me),
  })

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    submit.mutate()
  }

  return (
    <main className="relative flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="absolute right-4 top-4"><ThemeToggle /></div>
      <Card className="w-full max-w-sm">
        <h1 className="text-2xl font-semibold tracking-tight">Stock Simulator</h1>
        <p className="mt-1 text-sm text-slate-600">
          {mode === 'login' ? 'Log in to your account.' : 'Create an account and get $100,000 in virtual cash.'}
        </p>
        <form onSubmit={onSubmit} className="mt-6 space-y-3">
          <label className="block text-sm">
            Email
            <input className={inputClass} type="email" required autoComplete="email" value={email}
              onChange={(e) => setEmail(e.target.value)} />
          </label>
          {mode === 'register' && (
            <label className="block text-sm">
              Display name
              <input className={inputClass} required maxLength={100} value={displayName}
                onChange={(e) => setDisplayName(e.target.value)} />
            </label>
          )}
          <label className="block text-sm">
            Password
            <input className={inputClass} type="password" required minLength={mode === 'register' ? 8 : 1}
              maxLength={72} autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              value={password} onChange={(e) => setPassword(e.target.value)} />
          </label>
          <ErrorBox error={submit.error} />
          <button type="submit" className={`${buttonClass} w-full`} disabled={submit.isPending}>
            {mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>
        <button
          type="button"
          className="mt-4 text-sm text-slate-600 underline"
          onClick={() => {
            setMode(mode === 'login' ? 'register' : 'login')
            submit.reset()
          }}
        >
          {mode === 'login' ? 'No account? Sign up' : 'Have an account? Log in'}
        </button>
      </Card>
    </main>
  )
}
