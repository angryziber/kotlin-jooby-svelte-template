<script lang="ts">
  import router from './routing/Router'
  import session, {user} from './auth/Session'
  import gateway from '@ui/api/Gateway'
  import type {User} from '@ui/api/types'
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
