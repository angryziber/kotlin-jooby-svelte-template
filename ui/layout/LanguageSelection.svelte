<script>
  import {langs, rememberLang} from '@ui/i18n'

  const onLanguageSelection = (e) => {
    rememberLang(langs[e.target.selectedIndex])
  }
</script>

<div className="language-selection">
  <select className="custom-control custom-select" on:change={onLanguageSelection}>
    {#each Object.keys($langs) as lang}
      <option value={lang}>{lang.toUpperCase()}</option>
    {/each}
  </select>
</div>
