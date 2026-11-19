import { useMutation, useQuery } from '@tanstack/react-query'
import { api, ApiError } from './api'
import type { Me } from './types'

export function useMe() {
  return useQuery({
    queryKey: ['me'],
    queryFn: async () => {
      try {
        return await api<Me>('/api/auth/me')
      } catch (e) {
        if (e instanceof ApiError && e.status === 401) return null
        throw e
      }
    },
    staleTime: Infinity,
  })
}

/**
 * Logs out, then loads the start page fresh: that shows the login form and drops all cached data and the live
 * connection. Also done when the request fails (e.g. the session had already expired).
 */
export function useLogout() {
  return useMutation({
    mutationFn: () => api<void>('/api/auth/logout', { method: 'POST' }),
    onSettled: () => window.location.replace('/'),
  })
}
