import './shared/ArrayExtensions'
import sinon, {fake} from 'sinon'
import sinonChai from 'sinon-chai'
import chai from 'chai'
import {MochaOptions} from 'mocha'

chai.use(sinonChai)

window.fetch = fake.returns(new Promise(() => {}))

window['__WTR_CONFIG__'].testFrameworkConfig = {
  rootHooks: {
    afterEach: () => sinon.restore()
  }
} as MochaOptions
