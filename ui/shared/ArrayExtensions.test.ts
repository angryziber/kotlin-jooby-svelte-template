import './ArrayExtensions'

describe('Array extensions', () => {
  test('first', () => {
    expect([1, 2].first()).to.equal(1)
    expect([].first()).to.equal(undefined)
  })

  test('last', () => {
    expect([1, 2].last()).to.equal(2)
    expect([].last()).to.equal(undefined)
  })

  test('sum', () => {
    expect([1, 2].sum()).to.equal(3)
    expect([{x: 1}, {x: 2}].sum(e => e.x)).to.equal(3)
  })

  test('max', () => {
    expect([1, 3, 5].max()).to.equal(5)
    expect([{x: 1}, {x: 10}, {x: 5}].max(e => e.x)).to.equal(10)
    expect([].max()).to.equal(undefined)
    expect([].max(e => e['x'])).to.equal(undefined)
  })

  test('min', () => {
    expect([1, 3, 5].min()).to.equal(1)
    expect([{x: -10}, {x: 10}, {x: 5}].min(e => e.x)).to.equal(-10)
    expect([].min()).to.equal(undefined)
    expect([].min(e => e['x'])).to.equal(undefined)
  })

  test('indexBy', () => {
    expect([{x: 10}, {x: 5}].indexBy(e => e.x)).to.deep.equal({10: {x:10}, 5: {x: 5}})
    expect([{x: 10}, {x: 10}, {x: 5}].indexBy(e => e.x, (r, e) => r + e.x, 0)).to.deep.equal({'10': 20, '5': 5})
  })

  test('groupBy', () => {
    expect([{x: 10}, {x: 10}, {x: 5}].groupBy(e => e.x)).to.deep.equal({'10': [{x: 10}, {x: 10}], '5': [{x: 5}]})
  })

  test('countBy', () => {
    expect([{x: 10}, {x: 10}, {x: 5}].countBy(e => e.x)).to.deep.equal({'10': 2, '5': 1})
  })

  test('Array iteration after extension', () => {
    const a = ['x']
    // for (let i in a) expect(i).to.equal('0') - old-school iteration is broken
    for (let v of a) expect(v).to.equal('x')
    a.forEach((v, i) => {
      expect(i).to.equal(0)
      expect(v).to.equal('x')
    })
  })
})
