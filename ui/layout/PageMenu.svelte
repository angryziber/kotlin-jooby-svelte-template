<script lang="ts">
  import {_} from '@ui/i18n'
  import {user} from '../auth/Session'
  import {Role} from '@ui/api/types'

  class MenuItem {
    constructor(public subPage: string, public title: string) {}
    get page() {
      return `${$user!.role.toLowerCase()}${this.subPage ? '/' + this.subPage : ''}`
    }
  }

  const menu =
    $user!.role === Role.ADMIN ? [
      new MenuItem('', 'admin.dashboard.title')
    ] :
    $user!.role === Role.USER ? [
      new MenuItem('', 'user.welcome.menu')
    ] : []

  let url = location.pathname
</script>

<svelte:window on:popstate={() => url = location.pathname}/>

<ul class="nav navbar-nav">
  {#each menu as item}
    {#if item}
      <li class="nav-item" class:active={url.endsWith(item.page)}>
        <a class="nav-link" href="app:{item.page}">{$_(item.title)}</a>
      </li>
    {/if}
  {/each}
</ul>
