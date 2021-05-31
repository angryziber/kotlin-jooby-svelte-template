import gateway from './api/Gateway'
import {showToast} from '@ui/shared/toastStore'
import {get} from 'svelte/store'
import {_} from '@ui/i18n'

export function jsErrorHandler(message, source, line, column, error) {
  gateway.post('/api/js-error', {
    message, source, line, column,
    href: location.href,
    userAgent: navigator.userAgent,
    stack: error?.stack
  }).catch(e => console.error(e))
  alert('Technical error occurred, please reload the page:\n' + message)
}

export function handleUnhandledRejection(e: PromiseRejectionEvent) {
  console.error(e)
  if (e.reason?.stack) {
    jsErrorHandler(e.reason.message, undefined, undefined, undefined, e.reason)
    return
  }
  let error = e.reason?.message
  const $_ = get(_)
  if (error) {
    if (error === 'errors.apiVersionMismatch') {
      alert($_(error))
      return location.reload()
    }
    error = $_(e.reason?.message)
  } else error = $_('errors.technical') + ': ' + e.reason
  showToast(error, {type: 'danger'})
}

export function initErrorHandlers() {
  window.onerror = jsErrorHandler
  window.addEventListener('unhandledrejection', handleUnhandledRejection)
}
