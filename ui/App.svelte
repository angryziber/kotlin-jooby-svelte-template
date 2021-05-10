<script lang="ts">
  import {_} from '@ui/i18n'
  import {onDestroy, onMount} from 'svelte'
  import jsErrorHandler from './jsErrorHandler'
  import router from './routing/Router'
  import session, {user} from './auth/Session'
  import gateway from '@ui/api/Gateway'
  import type {User} from '@ui/api/types'
  import {showToast} from './shared/toastStore'
  import LoginPage from './pages/LoginPage.svelte'
  import NotFound from './pages/NotFound.svelte'
  import AdminDashboardPage from './pages/admin/AdminDashboardPage.svelte'
  import PageLayout from './layout/PageLayout.svelte'
  import UserWelcomePage from './pages/user/UserWelcomePage.svelte'
  import LoginLayout from './layout/LoginLayout.svelte'
  import Toast from './shared/Toasts.svelte'

  let page: string, pageParams = {}, isPublicPage = false

  function onPageChanged() {
    page = router.currentPage(location.pathname)
    isPublicPage = page === 'login'
  }

  function matches(page: string, path: string) {
    return pageParams = router.matches(path, page)
  }

  function handleError(e: PromiseRejectionEvent) {
    console.error(e)
    if (e.reason?.stack) {
      jsErrorHandler(e.reason.message, undefined, undefined, undefined, e.reason)
      return
    }
    let error = e.reason?.message
    if (error) {
      if (error === 'errors.apiVersionMismatch') {
        alert($_(error))
        return location.reload()
      }
      error = $_(e.reason?.message)
    }
    else error = $_('errors.technical') + ': ' + e.reason
    showToast(error, {type: 'danger'})
    if (e.reason.statusCode === 403)
      setTimeout(() => router.navigateWithReload('/logout'), 1000)
  }

  onMount(() => window.addEventListener('unhandledrejection', handleError))
  onDestroy(() => window.removeEventListener('unhandledrejection', handleError))

  async function init() {
    try {
      session.user = await gateway.get('/api/user') as User
    } catch (e) {
      session.user = null
      console.error(e)
    }

    onPageChanged()
    window.addEventListener('popstate', onPageChanged)

    if (!page && $user) router.navigateTo($user.role.toLowerCase(), {replaceHistory: true})
    else if (!page || !$user && !isPublicPage) router.navigateTo('login', {replaceHistory: true})
    else if (!isPublicPage && !page.startsWith($user?.role?.toLowerCase() ?? '')) page = ''
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
