import {fireEvent, render} from '@testing-library/svelte'
import SortableTable from './SortableTable.svelte'
import {$_, prop} from '../test-utils'

it('sorts items by clicking on headers', async () => {
  const items = [{a: 2, b: 'zzz'}, {a: 1, b: 'aaa'}]
  const key = 'admin.events'
  const columns = ['id', 'time']
  const fields = ['a', 'b']
  const {container, component} = render(SortableTable, {items, key, columns, fields, tableClass: 'another-class'})
  expect(container.querySelector('table')!.classList.contains('another-class')).to.be.ok
  expect(container).toContainHTML($_('admin.events.id'))
  expect(container).toContainHTML($_('admin.events.time'))
  expect(prop(component, 'items')).to.equal(items)

  const headers = container.querySelectorAll('th')
  await fireEvent.click(headers[0])
  expect(headers[0]).toContainHTML('▴')
  expect(prop(component, 'items')).to.deep.equal(items)

  await fireEvent.click(headers[0])
  expect(headers[0]).toContainHTML('▾')
  expect(prop(component, 'items')).to.deep.equal(items.reverse())
})
