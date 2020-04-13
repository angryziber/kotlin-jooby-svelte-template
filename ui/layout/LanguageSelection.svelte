<script>
  import {locales} from 'svelte-i18n'
  import {rememberLang} from '../i18n'

  const onLanguageSelection = (e) => {
    rememberLang($locale = $locales[e.target.selectedIndex])
  }
</script>

<div class="language-selection">
  <select class="custom-control custom-select" bind:value={$locale} on:change={onLanguageSelection}>
    {#each $locales as lang}
      <option value={lang}>{lang.toUpperCase()}</option>
    {/each}
  </select>
</div>
