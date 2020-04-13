<script>
  export let tableClass = ''
  export let items, key, columns
  export let fields = columns

  let prevBy, asc = 1

  function sortBy(array, by) {
    if (prevBy === by) asc = -asc; else asc = 1
    prevBy = by
    return array.sort((a, b) => {
      const bya = a[by], byb = b[by]
      if (bya === byb) return 0
      else if (bya > byb) return asc
      else return -asc
    })
  }
</script>

<style>
  th {
    cursor: pointer;
  }
</style>

<table class="table {tableClass}">
  <thead>
    <tr>
      {#each columns as column, i}
        <th on:click={() => items = sortBy(items, fields[i])}>
          {#if column}
            {$_(key + '.' + column)}
            {#if prevBy === fields[i]}
              <span class="arrow">{asc === 1 ? '▴' : '▾'}</span>
            {/if}
          {/if}
        </th>
      {/each}
    </tr>
  </thead>
  <tbody>
    <slot/>
  </tbody>
</table>

