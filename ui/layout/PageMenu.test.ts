import PageMenu from './PageMenu.svelte'
import session, {User} from '../auth/Session'
import {render} from '@testing-library/svelte'

const user = {id: '123', role: '?'} as User

it('renders admin menu', () => {
  session.user = {...user, role: 'admin'} as User
  const {container} = render(PageMenu)

  expect(container).toContainHTML('<a class="nav-link" href="app:admin">Dashboard</a>')
  expect(container).not.toContainHTML('<a class="nav-link" href="app:user">Dashboard</a>')
})

it('renders user menu', () => {
  session.user = {...user, role: 'user'} as User
  const {container} = render(PageMenu)

  expect(container).toContainHTML('<a class="nav-link" href="app:user">Overview</a>')
  expect(container).not.toContainHTML('<a class="nav-link" href="app:admin">Overview</a>')
})
