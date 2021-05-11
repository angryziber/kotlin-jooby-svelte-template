<script>
  import {_} from '@ui/i18n'
  import {onMount} from 'svelte'
  import {slide} from 'svelte/transition'
  import Icon from '../shared/Icon.svelte'
  import gateway from '../api/Gateway'
  import {finishLogin} from '../auth/Session'

  export let data = {login: '', password: ''}

  let errorMessage

  onMount(() => {
    if (location.hash) data.login = location.hash.substring(1)
  })

  async function submit() {
    try {
      const user = await gateway.post('/api/auth/login', data)
      if (user.role) await finishLogin(user)
      else errorMessage = 'login.failed'
    } catch (e) {
      errorMessage = e.message
    }
  }
</script>

<h4 class="mb-4">{$_('login.title')}</h4>

<form on:submit|preventDefault={submit}>
  {#if errorMessage}
    <div class="alert alert-danger">{$_(errorMessage)}</div>
  {/if}

  <div class="mb-3">
    <label class="form-label">{$_('login.login')}</label>
    <input type="text" name="login" bind:value={data.login} class="form-control form-control-lg" required autofocus>
  </div>

  <div class="mb-3" transition:slide|local>
    <label class="form-label">{$_('login.password')}</label>
    <input bind:value={data.password} type="password" name="password" class="form-control form-control-lg" required>
  </div>

  <p class="text-muted">{$_('login.safetyReminder')}</p>

  <button class="btn btn-lg btn-primary w-100">
    <span>{$_('login.submit')}</span>
    <Icon name="arrow-right"/>
  </button>
</form>
