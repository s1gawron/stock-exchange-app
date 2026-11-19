// Small fetch wrapper: same-origin session cookie + Spring Security SPA CSRF (XSRF-TOKEN cookie -> header).

export class ApiError extends Error {
  readonly status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

function readCookie(name: string): string | undefined {
  return document.cookie
    .split('; ')
    .find((c) => c.startsWith(name + '='))
    ?.split('=')[1]
}

async function csrfToken(): Promise<string> {
  let token = readCookie('XSRF-TOKEN')
  if (!token) {
    await fetch('/api/auth/csrf', { credentials: 'same-origin' })
    token = readCookie('XSRF-TOKEN')
  }
  return decodeURIComponent(token ?? '')
}

export async function api<T>(
  path: string,
  options: { method?: string; body?: unknown; headers?: Record<string, string> } = {},
): Promise<T> {
  const method = options.method ?? 'GET'
  const headers: Record<string, string> = { Accept: 'application/json', ...options.headers }
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'
  if (method !== 'GET') headers['X-XSRF-TOKEN'] = await csrfToken()

  const res = await fetch(path, {
    method,
    headers,
    credentials: 'same-origin',
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })
  // Session gone (expired, backend restarted, logged out in another tab): back to the login form.
  // Auth endpoints handle their own 401 (wrong password, "not logged in" check).
  if (res.status === 401 && !path.startsWith('/api/auth/')) {
    window.location.replace('/')
    throw new ApiError(401, 'Your session has ended. Please log in again.')
  }
  if (!res.ok) {
    let message = res.statusText || `HTTP ${res.status}`
    try {
      const problem = (await res.json()) as { detail?: string; title?: string }
      message = problem.detail ?? problem.title ?? message
    } catch {
      // no JSON body
    }
    throw new ApiError(res.status, message)
  }
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}
