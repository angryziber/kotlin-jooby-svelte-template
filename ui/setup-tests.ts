import './shared/ArrayExtensions'
import {fake} from 'sinon'
import sinonChai from 'sinon-chai'
import chai from 'chai'

chai.use(sinonChai)

window.fetch = fake.returns(new Promise(() => {}))
