import gateway from './api/Gateway'

export default function jsErrorHandler(message, source, line, column, error) {
  gateway.post('/js-error', {
    message, source, line, column,
    href: location.href,
    userAgent: navigator.userAgent,
    stack: error?.stack
  }).catch(e => console.error(e))
  alert('Technical error occurred, please reload the page:\n' + message)
}
