import router from './Router'
import {expect} from 'chai'
import {fake, SinonStub, stub} from 'sinon'

describe('router', () => {
  it('empty path', () => {
    expect(router.currentPage('/')).to.equal('')
    expect(router.currentPage('/en/app/')).to.equal('')
  })

  it('extracts current page path', () => {
    expect(router.currentPage('/en/app/login/')).to.equal('login/')
  })

  it('extracts nested paths', () => {
    expect(router.currentPage('/en/app/admin/myapp/tags/')).to.equal('admin/myapp/tags/')
  })

  it('fires "popstate" event', () => {
    let event
    window.addEventListener('popstate', e => { event = e })
    router.navigateTo('admin')
    expect(event).to.be.ok
  })

  it('pushes to history', () => {
    const push = stub(history, 'pushState')
    router.navigateTo('admin')
    expect(push).calledOnce
  })

  it('keeps /app/ prefix when navigating', () => {
    history.pushState(null, '', '/en/app/login/')

    const push = stub(history, 'pushState')
    router.navigateTo('admin/myapp/tags')

    expect(push).calledWith(null, '', '/en/app/admin/myapp/tags')
  })

  it('generates full url', () => {
    expect(router.fullUrl('user/blah', 'https://anonima.jp')).to.equal('https://anonima.jp/en/app/user/blah')
  })
})

describe('match', () => {
  it('exact', () => {
    expect(router.matches('hello/', 'hello/')).to.be.ok
  })

  it('empty', () => {
    expect(router.matches('login', '')).to.be.false
  })

  it('different length', () => {
    expect(router.matches('hello/a/', 'hello/')).to.be.false
    expect(router.matches('hello/a', 'hello/')).to.be.false
    expect(router.matches('hello/a', 'hello/a/blah')).to.be.false
  })

  it('path params', () => {
    expect(router.matches('hello/:tagId', 'hello/123')).to.deep.equal({tagId: '123'})
    expect(router.matches('hello/:p1/something/:p2', 'hello/1/something/2')).to.deep.equal({p1: '1', p2: '2'})
  })

  it('not matching params', () => {
    expect(router.matches('hello/:tagId', 'hello/')).to.be.false
    expect(router.matches('hello/:tagId', 'hello2/123')).to.be.false
  })
})

describe('global click', () => {
  let preventDefault, navigateTo: SinonStub
  beforeEach(() => {
    preventDefault = fake.returns('')
    navigateTo = stub(router, 'navigateTo')
  })

  it('ignores non-app hrefs', () => {
    window['clickWentThrough'] = false
    document.body.innerHTML = '<a href="javascript:window.clickWentThrough = true">Should navigate</a>'
    router.handleGlobalClick({target: document.querySelector('a')} as MouseEvent)
    expect(delete window['clickWentThrough']).to.equal(true)
    expect(navigateTo).callCount(0)
  })

  it('handles direct a[href]', () => {
    document.body.innerHTML = '<a href="app:page/page">Should navigate</a>'
    router.handleGlobalClick({target: document.querySelector('a'), preventDefault} as MouseEvent)
    expect(preventDefault).calledOnce
    expect(navigateTo).calledWith('page/page')
  })

  it('handles child of a[href], e.g. icon', () => {
    document.body.innerHTML = '<a href="app:page/page"><i>icon</i></a>'
    router.handleGlobalClick({target: document.querySelector('i'), preventDefault} as MouseEvent)
    expect(preventDefault).calledOnce
    expect(navigateTo).calledWith('page/page')
  })

  it('does not break on elements without hrefs', () => {
    document.body.innerHTML = '<div><i>icon</i></div>'
    router.handleGlobalClick({target: document.querySelector('i'), preventDefault} as MouseEvent)
    expect(preventDefault).not.called
    expect(navigateTo).not.called
  })
})
