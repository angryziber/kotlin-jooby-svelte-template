<script>
  import LanguageSelection from '../layout/LanguageSelection.svelte'
  import AppLogo from '../layout/AppLogo.svelte'
</script>

<div class="container-fluid">
  <div class="d-flex justify-content-between my-4">
    <AppLogo/>
    <LanguageSelection/>
  </div>

  <slot/>
</div>
