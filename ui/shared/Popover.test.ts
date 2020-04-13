import {render} from '@testing-library/svelte'
import Popover from './Popover.svelte'

test('Popover is shown', async () => {
  const {container, component} = render(Popover, {props: {title: 'Title', text: 'Text', containerClass: 'container'}})
  expect(container.querySelector('.popover')).toBeFalsy()
  jest.spyOn(container.querySelector('span.container')!, 'getBoundingClientRect').mockReturnValue({x: 20, y: 30} as any)

  await component.showPopover()

  const popover = document.querySelector('body > .popover') as HTMLElement
  expect(popover).toContainHTML('Title')
  expect(popover).toContainHTML('Text')
  expect(popover.style.top).toBe('30px')
})
