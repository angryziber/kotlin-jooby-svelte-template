import {render} from '@testing-library/svelte'
import gateway from '../../api/Gateway'
import UserWelcomePage from './UserWelcomePage.svelte'
import {$_} from '@ui/test-utils'
import {expect} from 'chai'
import {stub} from 'sinon'

it('renders', () => {
  const get = stub(gateway, 'get').resolves({hasNewMessage: false})
  const {container} = render(UserWelcomePage)
  expect(container.innerHTML).to.contain($_('user.welcome.title'))
  get.restore()
})
