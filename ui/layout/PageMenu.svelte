<script lang="ts">
  import {_} from '@ui/i18n'
  import {user} from '../auth/Session'

  class MenuItem {
    constructor(public subPage: string, public title: string) {}
    get page() {
      return `${$user!.role}${this.subPage ? '/' + this.subPage : ''}`
    }
  }

  const menu =
    $user!.role === 'admin' ? [
      new MenuItem('', 'admin.dashboard.title')
    ] :
    $user!.role === 'user' ? [
      new MenuItem('', 'user.welcome.menu')
    ] : []

  let url = location.pathname
</script>

<svelte:window on:popstate={() => url = location.pathname}/>

<ul class="nav navbar-nav flex-row flex-md-column mt-5">
  {#each menu as item}
    {#if item}
      <li class="nav-item" class:active={url.endsWith(item.page)}>
        <a class="nav-link" href="app:{item.page}">{$_(item.title)}</a>
      </li>
    {/if}
  {/each}
</ul>
