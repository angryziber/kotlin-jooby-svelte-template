<script>
  import Icon from './Icon.svelte'

  export let basePage, key, section, sections
</script>

<ul class="nav nav-pills my-4 my-lg-5">
  {#each Object.entries(sections) as [name, s]}
    <li class="nav-item mr-2">
      <a class="nav-link" class:active={section === name} href="app:{basePage}{name ? '/' + name : ''}">
        <Icon iconClass="mr-1" name={s.icon}/>
        {$_(key + '.' + name + '.title')}
      </a>
    </li>
  {/each}
</ul>
