import * as i18n from './i18n'

i18n.init()

window.fetch = jest.fn().mockReturnValue(new Promise(() => {}))

global['scrollTo'] = jest.fn()
