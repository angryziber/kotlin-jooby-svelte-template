import PageMenu from './PageMenu.svelte'
import session, {User} from '@ui/auth/Session'
import {render} from '@testing-library/svelte'
import {expect} from 'chai'

const user = {id: '123', role: '?'} as User

it('renders admin menu', () => {
  session.user = {...user, role: 'admin'} as User
  const {container} = render(PageMenu)

  expect(container.querySelector('[href="app:admin"]')).to.be.ok
  expect(container.querySelector('[href="app:user"]')).to.be.not.ok
})

it('renders user menu', () => {
  session.user = {...user, role: 'user'} as User
  const {container} = render(PageMenu)

  expect(container.querySelector('[href="app:user"]')).to.be.ok
  expect(container.querySelector('[href="app:admin"]')).to.be.not.ok
})
