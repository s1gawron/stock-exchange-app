import { useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../lib/api'
import type { Order } from '../lib/types'

export function CancelButton({ order }: { order: Order }) {
  const qc = useQueryClient()
  const cancel = useMutation({
    mutationFn: () => api<Order>(`/api/orders/${order.id}/cancel`, { method: 'POST' }),
    onSettled: () => {
      qc.invalidateQueries({ queryKey: ['orders'] })
      qc.invalidateQueries({ queryKey: ['portfolio'] })
    },
  })
  return (
    <button type="button" onClick={() => cancel.mutate()} disabled={cancel.isPending}
      title={cancel.error instanceof Error ? cancel.error.message : undefined}
      className="rounded border border-slate-300 px-2 py-0.5 text-xs hover:bg-slate-100 disabled:opacity-50">
      {cancel.isError ? 'Retry cancel' : 'Cancel'}
    </button>
  )
}
