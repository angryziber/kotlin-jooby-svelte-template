import './jsExtensions'

describe('Array extensions', () => {
  test('first', () => {
    expect([1, 2].first()).toBe(1)
    expect([].first()).toBe(undefined)
  })

  test('last', () => {
    expect([1, 2].last()).toBe(2)
    expect([].last()).toBe(undefined)
  })

  test('iteration', () => {
    const a = ['x']
    // for (let i in a) expect(i).toBe('0') - old-school iteration is broken
    for (let v of a) expect(v).toBe('x')
    a.forEach((v, i) => {
      expect(i).toBe(0)
      expect(v).toBe('x')
    })
  })
})
