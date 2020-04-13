<script>
  import Icon from '../../shared/Icon.svelte'
  import router from '../../routing/Router'
  import {onMount} from 'svelte'
  import session from '../../auth/Session'
  import gateway from '../../api/Gateway'

  const adminSections = {
    companies: {
      href: 'app:admin/companies',
    },
    users: {
      href: 'app:admin/users'
    },
    tags: {
      href: 'app:admin/tags'
    }
  }

  onMount(async () => {
    const stats = await gateway.get('/api/admins/stats')
    Object.keys(stats).forEach(k => adminSections[k].count = stats[k])
  })
</script>

<div class="container-fluid">
  <div class="container-header">
    <h2>{$_('admin.dashboard.title')}</h2>
  </div>

  <div class="card-deck">
    {#each Object.entries(adminSections) as [key, s]}
      <div class="card">
        <div class="card-body p-4 d-flex flex-column justify-content-between">

          <h5 class="card-title mb-0">{$_('admin.dashboard.' + key)} </h5>
          <h3 class="my-3">{s.count || '?'}</h3>
          <p class="text-muted">{$_('admin.dashboard.' + key + 'Description')}</p>

          {#if s.href}
            <a class="btn btn-outline-primary btn-with-icon" href={s.href}>
              {$_('admin.dashboard.manage')}
              <Icon name="settings"/>
            </a>
          {/if}
        </div>
      </div>
    {/each}
  </div>
</div>
