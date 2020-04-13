import session, {finishLogin, User} from './Session'
import router from '../routing/Router'

const user: User = {
  id: 'id', role: 'user', login: 'login'
}

describe('finishLogin', () => {
  beforeEach(() => {
    jest.spyOn(router, 'navigateTo')
  })

  it('redirects to user.role', async () => {
    await finishLogin(user)
    expect(session.user).toEqual(user)
    expect(router.navigateTo).toBeCalledWith(user.role)
  })

  it('redirects to provided page', async () => {
    await finishLogin(user, 'some/page')
    expect(session.user).toEqual(user)
    expect(router.navigateTo).toBeCalledWith('some/page')
  })
})
