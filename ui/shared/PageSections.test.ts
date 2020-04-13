import {render} from '@testing-library/svelte'
import PageSections from './PageSections.svelte'

it('renders', async () => {
  const sections = {
    'wb': {icon: 'tag'},
    'detach': {icon: 'settings'}
  }

  const {container} = render(PageSections, {sections, section: 'detach', basePage: 'user/page', key: 'company.invitations'})
  expect(container).toContainHTML('<a class="nav-link" href="app:user/page/wb">')
  expect(container).toContainHTML('<a class="nav-link active" href="app:user/page/detach">')
})
