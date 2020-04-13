import gateway from './api/Gateway'

export default async function jsErrorHandler(message, source, line, column, error) {
  try {
    await gateway.post('/js-error', {
      message, source, line, column,
      href: location.href,
      userAgent: navigator.userAgent,
      stack: error && error.stack
    })
  } catch (e) {
    console.error(e)
  }
}
