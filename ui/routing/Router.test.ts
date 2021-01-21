import router from './Router'

describe('router', () => {
  it('empty path', () => {
    expect(router.currentPage('/')).toBe('')
    expect(router.currentPage('/en/app/')).toBe('')
  })

  it('extracts current page path', () => {
    expect(router.currentPage('/en/app/login/')).toBe('login/')
  })

  it('extracts nested paths', () => {
    expect(router.currentPage('/en/app/admin/myapp/tags/')).toBe('admin/myapp/tags/')
  })

  it('fires "popstate" event', () => {
    let event
    window.addEventListener('popstate', e => { event = e })
    router.navigateTo('admin')
    expect(event).toBeTruthy()
  })

  it('pushes to history', () => {
    const push = spyOn(history, 'pushState')
    router.navigateTo('admin')
    expect(push).toBeCalledTimes(1)
  })

  it('keeps /app/ prefix when navigating', () => {
    history.pushState(null, '', '/en/app/login/')

    const push = spyOn(history, 'pushState')
    router.navigateTo('admin/myapp/tags')

    expect(push).toHaveBeenCalledWith(null, '', '/en/app/admin/myapp/tags')
  })

  it('generates full url', () => {
    expect(router.fullUrl('user/blah', 'https://anonima.jp')).toBe('https://anonima.jp/en/app/user/blah')
  })
})

describe('match', () => {
  it('exact', () => {
    expect(router.matches('hello/', 'hello/')).toBeTruthy()
  })

  it('empty', () => {
    expect(router.matches('login', '')).toBeFalsy()
  })

  it('different length', () => {
    expect(router.matches('hello/a/', 'hello/')).toBeFalsy()
    expect(router.matches('hello/a', 'hello/')).toBeFalsy()
    expect(router.matches('hello/a', 'hello/a/blah')).toBeFalsy()
  })

  it('path params', () => {
    expect(router.matches('hello/:tagId', 'hello/123')).toEqual({tagId: '123'})
    expect(router.matches('hello/:p1/something/:p2', 'hello/1/something/2')).toEqual({p1: '1', p2: '2'})
  })

  it('not matching params', () => {
    expect(router.matches('hello/:tagId', 'hello/')).toBeFalsy()
    expect(router.matches('hello/:tagId', 'hello2/123')).toBeFalsy()
  })
})

describe('global click', () => {
  let preventDefault

  beforeEach(() => {
    preventDefault = jest.fn() as Function
    jest.spyOn(router, 'navigateTo')
  })

  it('ignores non-app hrefs', () => {
    window['clickWentThrough'] = false
    document.body.innerHTML = '<a href="javascript:window.clickWentThrough = true">Should navigate</a>'
    router.handleGlobalClick({target: document.querySelector('a')} as MouseEvent)
    expect(delete window['clickWentThrough']).toBe(true)
    expect(router.navigateTo).not.toBeCalled()
  })

  it('handles direct a[href]', () => {
    document.body.innerHTML = '<a href="app:page/page">Should navigate</a>'
    router.handleGlobalClick({target: document.querySelector('a'), preventDefault} as MouseEvent)
    expect(preventDefault).toBeCalled()
    expect(router.navigateTo).toBeCalledWith('page/page')
  })

  it('handles child of a[href], e.g. icon', () => {
    document.body.innerHTML = '<a href="app:page/page"><i>icon</i></a>'
    router.handleGlobalClick({target: document.querySelector('i'), preventDefault} as MouseEvent)
    expect(preventDefault).toBeCalled()
    expect(router.navigateTo).toBeCalledWith('page/page')
  })

  it('does not break on elements without hrefs', () => {
    document.body.innerHTML = '<div><i>icon</i></div>'
    router.handleGlobalClick({target: document.querySelector('i'), preventDefault} as MouseEvent)
    expect(preventDefault).not.toBeCalled()
    expect(router.navigateTo).not.toBeCalled()
  })
})
