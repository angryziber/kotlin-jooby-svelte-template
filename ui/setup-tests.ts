import './shared/ArrayExtensions'

window.fetch = jest.fn().mockReturnValue(new Promise(() => {}))

window['scrollTo'] = jest.fn()
window['config'] = {}

process.on('unhandledRejection', e => {
  throw e
})
