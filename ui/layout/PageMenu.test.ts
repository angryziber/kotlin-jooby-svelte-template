import PageMenu from './PageMenu.svelte'
import session from '@ui/auth/Session'
import {render} from '@testing-library/svelte'
import {Role, User} from '@ui/api/types'
import {expect} from 'chai'

const user = {id: '123', role: Role.ADMIN} as User

it('renders admin menu', () => {
  session.user = {...user, role: Role.ADMIN} as User
  const {container} = render(PageMenu)

  expect(container.querySelector('[href="app:admin"]')).to.be.ok
  expect(container.querySelector('[href="app:user"]')).to.be.not.ok
})

it('renders user menu', () => {
  session.user = {...user, role: Role.USER} as User
  const {container} = render(PageMenu)

  expect(container.querySelector('[href="app:user"]')).to.be.ok
  expect(container.querySelector('[href="app:admin"]')).to.be.not.ok
})
