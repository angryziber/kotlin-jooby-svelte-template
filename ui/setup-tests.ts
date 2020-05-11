import * as i18n from './i18n'
import './shared/jsExtensions'

i18n.init()

window.fetch = jest.fn().mockReturnValue(new Promise(() => {}))

beforeEach(() => jest.clearAllTimers())

process.on('unhandledRejection', e => {
  throw e
})
