// Theme: 'system' follows the OS setting; 'light'/'dark' are the user's override (saved per browser).
// index.html applies the saved theme before React starts, so the page does not flash.

export type Theme = 'system' | 'light' | 'dark'

const KEY = 'theme'
const media = () => window.matchMedia('(prefers-color-scheme: dark)')

export function storedTheme(): Theme {
  try {
    const v = localStorage.getItem(KEY)
    return v === 'light' || v === 'dark' ? v : 'system'
  } catch {
    return 'system' // storage blocked (private mode etc.)
  }
}

export function saveTheme(theme: Theme) {
  try {
    if (theme === 'system') localStorage.removeItem(KEY)
    else localStorage.setItem(KEY, theme)
  } catch {
    // not saved; the choice still applies until reload
  }
}

export function applyTheme(theme: Theme) {
  const dark = theme === 'dark' || (theme === 'system' && media().matches)
  document.documentElement.classList.toggle('dark', dark)
}

/** Re-applies 'system' when the OS setting changes. Returns an unsubscribe function. */
export function followSystem(getTheme: () => Theme): () => void {
  const m = media()
  const onChange = () => {
    if (getTheme() === 'system') applyTheme('system')
  }
  m.addEventListener('change', onChange)
  return () => m.removeEventListener('change', onChange)
}
