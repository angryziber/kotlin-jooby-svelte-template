import {render} from '@testing-library/svelte'
import session, {User} from '../../auth/Session'
import gateway from '../../api/Gateway'
import UserWelcomePage from './UserWelcomePage.svelte'
import {$_} from '../../test-utils'

beforeAll(() => {
  session.testUser = {role: 'user'} as User
})

it('renders', () => {
  jest.spyOn(gateway, 'get').mockResolvedValue({hasNewMessage: false})
  const {container} = render(UserWelcomePage)
  expect(container).toContainHTML($_('user.welcome.title'))
})
