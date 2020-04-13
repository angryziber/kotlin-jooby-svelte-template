import {fireEvent, render} from '@testing-library/svelte'
import LoginForm from './LoginForm.svelte'
import gateway from '../api/Gateway'
import router from '../routing/Router'
import session from '../auth/Session'
import {change} from '../test-utils'

beforeEach(() => {
  jest.spyOn(router, 'navigateTo')
})

afterEach(() => {
  session.user = null
})

test('successful login redirects to the role page', async () => {
  const data = {login: 'user', password: 'pass'}
  const {container} = render(LoginForm, {data})

  jest.spyOn(gateway, 'post').mockResolvedValue({role: 'admin'})
  await fireEvent.submit(container.querySelector('form')!)
  await change()
  expect(gateway.post).toBeCalledWith('/api/auth/login', data)
  expect(session.user).toEqual(expect.objectContaining({role: 'admin'}))
  expect(router.navigateTo).toBeCalledWith('admin')
})

test('login fails', async () => {
  const {container} = render(LoginForm)

  jest.spyOn(gateway, 'post').mockReturnValue(Promise.reject({message: 'failed'}))
  await fireEvent.submit(container.querySelector('form')!)

  expect(container.querySelector('.alert-danger')).toContainHTML('failed')
})
