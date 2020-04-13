import {render} from '@testing-library/svelte'
import Icon from './Icon.svelte'

test('Icon renders with name', () => {
  const {container} = render(Icon, {name: 'mega'})
  expect(container.querySelector('svg use')).toHaveAttribute('xlink:href', '/gen/svg/sprite.symbol.svg#mega')
})
