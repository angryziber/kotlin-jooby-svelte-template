<script>
  import LanguageSelection from '../layout/LanguageSelection.svelte'
  import AppLogo from '../layout/AppLogo.svelte'
</script>

<div class="login-container">
  <div class="login-header">
    <AppLogo/>
    <LanguageSelection/>
  </div>

  <slot/>
</div>
