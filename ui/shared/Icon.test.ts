import {render} from '@testing-library/svelte'
import Icon from './Icon.svelte'
import {expect} from 'chai'

it('Icon renders with name', () => {
  const {container} = render(Icon, {name: 'mega'})
  expect(container.querySelector('svg use')!.getAttribute('href')).to.equal('/gen/svg/sprite.symbol.svg#mega')
})
