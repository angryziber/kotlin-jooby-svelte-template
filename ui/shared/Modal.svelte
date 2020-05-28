<script>
  import { fade, fly } from 'svelte/transition'
  import {onDestroy} from 'svelte'

  export let title
  export let show = true
  export let modalClass = ''
  export let flyParams = {y: -500}
  let backdrop, modal

  $: if (backdrop) document.body.appendChild(backdrop)
  $: if (modal) document.body.appendChild(modal)

  $: {
    if (show)
      document.body.classList.add('modal-open')
    else
      document.body.classList.remove('modal-open')
  }

  onDestroy(() => {
    document.body.classList.remove('modal-open')
    setTimeout(() => {
      if (backdrop) backdrop.remove()
      if (modal) modal.remove()
    })
  })

  function close() {
    show = false
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
  <div bind:this={modal} class="modal" tabindex="-1" role="dialog">
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
        <slot name="footer"/>
      </div>
    </div>
  </div>

  <div bind:this={backdrop} class="modal-backdrop fade show" transition:fade={{duration: flyParams.duration}}></div>
{/if}
