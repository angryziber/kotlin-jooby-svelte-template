<script>
  import {onMount, onDestroy} from 'svelte'
  import router from './routing/Router'
  import session, {user} from './auth/Session'
  import {showToast} from './shared/toastStore'
  import LoginPage from './pages/LoginPage.svelte'
  import NotFound from './pages/NotFound.svelte'
  import AdminDashboardPage from './pages/admin/AdminDashboardPage.svelte'
  import PageLayout from './layout/PageLayout.svelte'
  import UserWelcomePage from './pages/user/UserWelcomePage.svelte'
  import LoginLayout from './layout/LoginLayout.svelte'
  import Toast from './shared/Toasts.svelte'

  export let initialUser = undefined

  let page, pageParams, isPublicPage

  function onPageChanged() {
    page = router.currentPage(location.pathname)
    isPublicPage = page === 'login'
  }

  function matches(page, path) {
    return pageParams = router.matches(path, page)
  }

  function handleError(e) {
    console.error(e)
    let error = e.message
    if (!error) {
      error = e.reason.message ? $_(e.reason.message) : $_('errors.technical') + ': ' + e.reason
    }
    showToast(error, {type: 'danger'})
    if (e.reason && e.reason.statusCode === 403)
      setTimeout(() => router.navigateWithReload('/logout'), 1000)
  }

  onMount(() => window.addEventListener('unhandledrejection', handleError))
  onDestroy(() => window.removeEventListener('unhandledrejection', handleError))

  async function init() {
    try {
      if (initialUser) session.user = initialUser
    } catch (e) {
      console.error(e)
      session.user = null
    }

    onPageChanged()
    window.addEventListener('popstate', onPageChanged)

    if (!page && $user) router.navigateTo($user.role)
    else if (!page || !$user && !isPublicPage) router.navigateTo('login', {replaceHistory: true})
    else if (!isPublicPage && !page.startsWith($user.role)) page = ''
  }

  init()
</script>

<Toast/>

{#if page !== undefined}
  <svelte:component this={isPublicPage ? LoginLayout : $user ? PageLayout : undefined}>
    {#if matches(page, 'login')}
      <LoginPage/>
    {:else if matches(page, 'admin')}
      <AdminDashboardPage/>
    {:else if matches(page, 'user')}
      <UserWelcomePage/>
    {:else}
      <NotFound/>
    {/if}
  </svelte:component>
{/if}
