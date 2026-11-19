import { useEffect, useRef, useState } from 'react'
import { applyTheme, followSystem, saveTheme, storedTheme, type Theme } from '../lib/theme'

const NEXT: Record<Theme, Theme> = { system: 'light', light: 'dark', dark: 'system' }
const LABEL: Record<Theme, string> = { system: '◐ System', light: '☀ Light', dark: '☾ Dark' }

/** Cycles System → Light → Dark. */
export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(storedTheme)
  const current = useRef(theme)

  useEffect(() => {
    current.current = theme
    applyTheme(theme)
  }, [theme])

  useEffect(() => followSystem(() => current.current), [])

  const next = NEXT[theme]
  return (
    <button type="button" onClick={() => { saveTheme(next); setTheme(next) }}
      title={`Theme: ${theme}. Click for ${next}.`} aria-label={`Theme: ${theme}. Switch to ${next}.`}
      className="rounded-md border border-slate-300 px-2.5 py-1 text-sm hover:bg-slate-100">
      {LABEL[theme]}
    </button>
  )
}
