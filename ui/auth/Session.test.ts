import session, {finishLogin} from './Session'
import router from '../routing/Router'
import {Role, User} from '@ui/api/types'
import {SinonStub, stub} from 'sinon'
import {expect} from 'chai'

const user = {id: 'id', role: Role.USER, login: 'login'} as User

describe('finishLogin', () => {
  let navigateTo: SinonStub
  beforeEach(() => navigateTo = stub(router, 'navigateTo'))
  afterEach(() => navigateTo.restore())

  it('redirects to user.role', async () => {
    await finishLogin(user)
    expect(session.user).to.eq(user)
    expect(router.navigateTo).calledWith(user.role.toLowerCase())
  })

  it('redirects to provided page', async () => {
    await finishLogin(user, 'some/page')
    expect(session.user).to.eq(user)
    expect(router.navigateTo).calledWith('some/page')
  })
})
