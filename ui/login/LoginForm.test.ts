import {fireEvent, render} from '@testing-library/svelte'
import LoginForm from './LoginForm.svelte'
import gateway from '../api/Gateway'
import router from '../routing/Router'
import session from '../auth/Session'
import {change} from '../test-utils'
import {SinonStub, stub} from 'sinon'
import {expect} from 'chai'
import {Role} from '@ui/api/types'

let post: SinonStub, navigateTo: SinonStub
beforeEach(() => {
  post = stub(gateway, 'post')
  navigateTo = stub(router, 'navigateTo')
})

afterEach(() => {
  post.restore()
  navigateTo.restore()
})

it('successful login redirects to the role page', async () => {
  const data = {login: 'user', password: 'pass'}
  const {container} = render(LoginForm, {data})

  post.resolves({role: Role.ADMIN})
  await fireEvent.submit(container.querySelector('form')!)
  await change()
  expect(post).calledWith('/api/auth/login', data)
  expect(session.user).to.have.property('role', Role.ADMIN)
  expect(navigateTo).calledWith('admin')
})

it('login fails', async () => {
  const {container} = render(LoginForm)

  post.rejects(new Error('failed'))
  await fireEvent.submit(container.querySelector('form')!)

  expect(container.querySelector('.alert-danger')!.textContent).to.contain('failed')
})
