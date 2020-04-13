 <script>
  import {tick} from 'svelte'
  export let title = undefined
  export let text = undefined
  export let containerClass = ''
  let position = 'top'
  let show = false, top = 0, left = 0, arrowLeft = 0
  let slotRef, popoverRef, arrowRef

  export async function showPopover() {
    show = true
    await tick()
    document.body.appendChild(popoverRef)
    const slotRect = slotRef.getBoundingClientRect()
    const popoverRect = popoverRef.getBoundingClientRect()
    const arrowRect = arrowRef.getBoundingClientRect()
    const popoverHeight = popoverRect.height + arrowRect.height

    top = slotRect.y - popoverHeight

    if (top < 0) {
      top = slotRect.y + slotRect.height / 2 + arrowRect.height
      position = 'bottom'
    }

    left = slotRect.x + slotRect.width / 2 - popoverRect.width / 2
    arrowLeft = popoverRect.width / 2 - arrowRect.width / 2
  }
</script>

<style>
  .arrow {
    margin: 0;
  }
</style>

<span class="{containerClass}" style="display: inline-block; position: relative"
    bind:this={slotRef}
    on:mouseenter={showPopover}
    on:mouseleave={() => show = false}>
  <slot/>
</span>

{#if show}
  <div bind:this={popoverRef} role="tooltip"
       class="popover bs-popover-{position}"
       style="top: {top}px; left: {left}px">
    <div class="arrow" bind:this={arrowRef} style="left: {arrowLeft}px"></div>
    <h3 class="popover-header">
      <slot name="title">{title}</slot>
    </h3>
    <div class="popover-body">
      <slot name="text">{text}</slot>
    </div>
  </div>
{/if}
