import {act, render} from '@testing-library/svelte'
import App from './App.svelte'
import router from './routing/Router'
import session, {User} from './auth/Session'
import {$_, change} from './test-utils'

const user = {id: '123-123', role: 'company'} as User

beforeEach(() => {
  document.body.innerHTML = '<div id="footer">Footer</div>'
  jest.spyOn(router, 'currentPage').mockReturnValue('company-test')
  jest.spyOn(router, 'navigateTo')
  session.user = null
})

it('uses user from initial state if exists', async () => {
  render(App, {initialUser: user})
  await change()
  expect(router.navigateTo).not.toBeCalled()
  expect(session.user).to.deep.equal(user)
})

it('navigates to login page if no user in session', () => {
  render(App)
  expect(router.navigateTo).calledWith('login', {replaceHistory: true})
})

it('shows role page when user in session', () => {
  session.user = user
  render(App)
  expect(router.navigateTo).not.toBeCalled()
})

it('shows public page even when a user is in session', async () => {
  session.user = user
  jest.spyOn(router, 'currentPage').mockReturnValue('login')

  const {container} = render(App)

  await change()
  expect(router.navigateTo).not.toBeCalled()
  expect(container).toContainHTML('Access')
})

it('shows not found when role does not match', async () => {
  session.user = user
  jest.spyOn(router, 'currentPage').mockReturnValue('admin/companies')

  const {container} = render(App)

  await change()
  expect(container).toContainHTML('Page Not Found')
})

it('navigates to user role page if there is a user in session', async () => {
  session.user = {...user, role: 'user'} as User
  jest.spyOn(router, 'currentPage').mockReturnValue('')

  render(App)

  await change()
  expect(router.navigateTo).calledWith('user')
})

describe('handles unhandled promises', () => {
  let container
  const e = new Event('unhandledrejection') as any

  beforeEach(() => {
    ({container} = render(App))
  })

  it('without translation', async () => {
    e.reason = {message: 'no translation', statusCode: 400}
    await act(() => window.dispatchEvent(e))
    expect(container).toContainHTML('no translation')
  })

  it('with translation', async () => {
    e.reason = {message: 'errors.technical', statusCode: 500}
    await act(() => window.dispatchEvent(e))
    expect(container).toContainHTML($_('errors.technical'))
  })

  it('unauthorized logs out', async () => {
    jest.useFakeTimers()
    jest.spyOn(router, 'navigateWithReload')
    e.reason = {message: 'no permissions', statusCode: 403}
    await act(() => window.dispatchEvent(e))
    expect(container).toContainHTML('no permissions')
    jest.runAllTimers()
    expect(router.navigateWithReload).calledWith('/logout')
    jest.useRealTimers()
  })
})
