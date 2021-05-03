import {act, render} from '@testing-library/svelte'
import App from './App.svelte'
import router from './routing/Router'
import session from './auth/Session'
import {$_, change} from './test-utils'
import {expect} from 'chai'
import {SinonStub, stub, useFakeTimers} from 'sinon'
import {Role, User} from '@ui/api/types'

const user = {id: '123-123', role: Role.ADMIN} as User

let currentPage: SinonStub, navigateTo: SinonStub

beforeEach(() => {
  document.body.innerHTML = '<div id="footer">Footer</div>'
  currentPage = stub(router, 'currentPage').returns('company-test')
  navigateTo = stub(router, 'navigateTo')
  session.user = null
})

afterEach(() => {
  currentPage.restore()
  navigateTo.restore()
})

it('uses user from initial state if exists', async () => {
  render(App, {initialUser: user})
  await change()
  expect(router.navigateTo).not.called
  expect(session.user).to.deep.equal(user)
})

it('navigates to login page if no user in session', () => {
  render(App)
  expect(router.navigateTo).calledWith('login', {replaceHistory: true})
})

it('shows role page when user in session', () => {
  session.user = user
  render(App)
  expect(router.navigateTo).not.called
})

it('shows public page even when a user is in session', async () => {
  session.user = user
  currentPage.returns('login')

  const {container} = render(App)

  await change()
  expect(router.navigateTo).not.called
  expect(container.innerHTML).to.contain('Access')
})

it('shows not found when role does not match', async () => {
  session.user = user
  currentPage.returns('admin/companies')

  const {container} = render(App)

  await change()
  expect(container.innerHTML).to.contain('Page Not Found')
})

it('navigates to user role page if there is a user in session', async () => {
  session.user = {...user, role: Role.USER} as User
  currentPage.returns('')

  render(App)

  await change()
  expect(router.navigateTo).calledWith(Role.USER)
})

describe('handles unhandled promises', () => {
  const promise = Promise.reject('')
  let container: HTMLElement

  beforeEach(() => {
    container = render(App).container
  })

  it('without translation', async () => {
    const e = new PromiseRejectionEvent('unhandledrejection', {reason: {message: 'no translation'}, promise})
    await act(() => window.dispatchEvent(e))
    expect(container.textContent).to.contain('no translation')
  })

  it('with translation', async () => {
    const e = new PromiseRejectionEvent('unhandledrejection', {reason: {message: 'errors.technical', statusCode: 500}, promise})
    await act(() => window.dispatchEvent(e))
    expect(container.textContent).to.contain($_('errors.technical'))
  })

  it('unauthorized logs out', async () => {
    const clock = useFakeTimers()
    const navigateWithReload = stub(router, 'navigateWithReload')
    const e = new PromiseRejectionEvent('unhandledrejection', {reason: {message: 'no permissions', statusCode: 403}, promise})
    await act(() => window.dispatchEvent(e))
    expect(container.textContent).to.contain('no permissions')
    clock.runAll()
    expect(router.navigateWithReload).calledWith('/logout')
    clock.restore()
    navigateWithReload.restore()
  })
})
