<script>
  import {_} from '@ui/i18n'
</script>

<div class="text-center pt-5">
  <h1 class="display-3 text-muted mt-5">404</h1>
  <h4 class="text-primary my-4">
    {$_('notfound.message')}
  </h4>

  <button class="btn btn-primary btn-lg mt-3" on:click={() => history.back()}>
    {$_('notfound.back')}
  </button>
</div>
