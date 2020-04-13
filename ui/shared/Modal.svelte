<script>
  import { afterUpdate } from 'svelte'
  import { fade, fly } from 'svelte/transition'
  export let title
  export let show = true
  export let modalClass = ''
  export let flyParams = {y: -500}
  export let squashFooter = false
  let backdrop

  afterUpdate(() => {
    backdrop && document.body.appendChild(backdrop)

    if (show) {
      document.body.classList.add('modal-open')
      squashFooter && moveElementsFromFooterSlotToParent()
    }
    else
      document.body.classList.remove('modal-open')
  })

  function moveElementsFromFooterSlotToParent() {
    const footer = document.querySelector('[slot="footer"]')
    if (footer) {
      const parent = footer.parentElement
      while (footer.childNodes.length > 0) {
        parent.appendChild(footer.childNodes[0])
      }
    }
  }

  function close() {
    show = false
  }

  function closeByBackdrop(e) {
    if (e.target.classList.contains('modal')) close()
  }

  function onKeyUp(e) {
    if (show && e.keyCode === 27) close()
  }
</script>

<style>
  .modal {
    display: block;
  }

  .modal-footer:empty {
    display: none;
  }
</style>

<svelte:window on:keyup={onKeyUp}/>

{#if show}
  <div class="modal" tabindex="-1" role="dialog" on:click={closeByBackdrop}>
    <div class="modal-dialog {modalClass}" role="document" transition:fly={flyParams}>
      <div class="modal-content">
        <div class="modal-header">
          <slot name="additional-header"/>
          <h4 class="modal-title">{title}</h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close" on:click={close}>
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <slot/>
        </div>
        <div class="modal-footer">
          <slot name="footer"/>
        </div>
      </div>
    </div>
  </div>

  <div bind:this={backdrop} class="modal-backdrop fade show" transition:fade={{duration: flyParams.duration}}></div>
{/if}
