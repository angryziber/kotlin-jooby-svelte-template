<script>
  export let name
  export let iconClass = ''
</script>

<svg class="icon icon-{name} {iconClass}" style="pointer-events: none">
  <use href="/gen/svg/sprite.symbol.svg#{name}"></use>
</svg>
