import './ArrayExtensions'

describe('Array extensions', () => {
  test('first', () => {
    expect([1, 2].first()).toBe(1)
    expect([].first()).toBe(undefined)
  })

  test('last', () => {
    expect([1, 2].last()).toBe(2)
    expect([].last()).toBe(undefined)
  })

  test('sum', () => {
    expect([1, 2].sum()).toBe(3)
    expect([{x: 1}, {x: 2}].sum(e => e.x)).toBe(3)
  })

  test('max', () => {
    expect([1, 3, 5].max()).toBe(5)
    expect([{x: 1}, {x: 10}, {x: 5}].max(e => e.x)).toBe(10)
    expect([].max()).toBe(undefined)
    expect([].max(e => e['x'])).toBe(undefined)
  })

  test('min', () => {
    expect([1, 3, 5].min()).toBe(1)
    expect([{x: -10}, {x: 10}, {x: 5}].min(e => e.x)).toBe(-10)
    expect([].min()).toBe(undefined)
    expect([].min(e => e['x'])).toBe(undefined)
  })

  test('indexBy', () => {
    expect([{x: 10}, {x: 5}].indexBy(e => e.x)).toEqual({10: {x:10}, 5: {x: 5}})
    expect([{x: 10}, {x: 10}, {x: 5}].indexBy(e => e.x, (r, e) => r + e.x, 0)).toEqual({'10': 20, '5': 5})
  })

  test('groupBy', () => {
    expect([{x: 10}, {x: 10}, {x: 5}].groupBy(e => e.x)).toEqual({'10': [{x: 10}, {x: 10}], '5': [{x: 5}]})
  })

  test('countBy', () => {
    expect([{x: 10}, {x: 10}, {x: 5}].countBy(e => e.x)).toEqual({'10': 2, '5': 1})
  })

  test('Array iteration after extension', () => {
    const a = ['x']
    // for (let i in a) expect(i).toBe('0') - old-school iteration is broken
    for (let v of a) expect(v).toBe('x')
    a.forEach((v, i) => {
      expect(i).toBe(0)
      expect(v).toBe('x')
    })
  })
})
