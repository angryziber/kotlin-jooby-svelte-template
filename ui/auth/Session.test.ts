import session, {finishLogin, User} from './Session'
import router from '../routing/Router'
import {SinonStub, stub} from 'sinon'
import {expect} from 'chai'

const user: User = {
  id: 'id', role: 'user', login: 'login'
}

describe('finishLogin', () => {
  let navigateTo: SinonStub
  beforeEach(() => navigateTo = stub(router, 'navigateTo'))
  afterEach(() => navigateTo.restore())

  it('redirects to user.role', async () => {
    await finishLogin(user)
    expect(session.user).to.eq(user)
    expect(router.navigateTo).calledWith(user.role)
  })

  it('redirects to provided page', async () => {
    await finishLogin(user, 'some/page')
    expect(session.user).to.eq(user)
    expect(router.navigateTo).calledWith('some/page')
  })
})
