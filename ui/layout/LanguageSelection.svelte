<script>
  import {lang, langs, rememberLang} from '@ui/i18n'
</script>

<div class="language-selection">
  <select class="form-select" bind:value={$lang} on:change={e => rememberLang(e.target.value)}>
    {#each Object.keys($langs) as lang}
      <option value={lang}>{lang.toUpperCase()}</option>
    {/each}
  </select>
</div>
