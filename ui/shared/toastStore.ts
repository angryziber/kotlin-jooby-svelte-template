import {writable} from 'svelte/store'

interface ToastOptions {
  title?: string
  type: string
  timeoutSec: number
  modal?: boolean
}

interface Toast extends ToastOptions {
  id: number
  message: string
}

export const toastStore = writable<Array<Toast>>([])

export function showToast(message: string, options?: Partial<ToastOptions>): Toast {
  const toast: Toast = {id: Math.random(), type: 'success', timeoutSec: 10, message, ...options}
  toastStore.update(a => [...a, toast])
  if (!toast.modal)
    setTimeout(() => hideToast(toast), toast.timeoutSec * 1000)
  return toast
}

export function hideToast(toast: Toast) {
  toastStore.update(a => a.filter(t => t !== toast))
}
