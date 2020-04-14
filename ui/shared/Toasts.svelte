<script>
  import {toastStore, hideToast} from './toastStore'
  import Modal from './Modal.svelte'

  function out(node) {
    node.classList.remove('slide-in-blurred-top')
    node.classList.add('slide-out-blurred-top')
    return {duration: 500}
  }
</script>

<div class="toasts-container">
  {#each $toastStore as toast (toast.id)}
    {#if !toast.modal}
      <div aria-atomic="true" aria-live="assertive" role="alert" class="toast slide-in-blurred-top toast-{toast.type}" out:out>
        <div class="toast-header">
          <strong class="mr-auto">{toast.title || toast.message}</strong>
          <button aria-label="Close" class="ml-2 mb-1 close" on:click={() => hideToast(toast)}>
            <span aria-hidden="true">×</span>
          </button>
        </div>
        {#if toast.title}
          <div class="toast-body" style="white-space: pre-wrap">
            {toast.message}
          </div>
        {/if}
      </div>
    {/if}
  {/each}
</div>

{#each $toastStore as toast (toast.id)}
  {#if toast.modal}
    <Modal modalClass="modal-dialog-centered" title={toast.title} show={true}>
      <div class="alert alert-{toast.type} font-weight-normal h5 mb-0" style="white-space: pre-wrap">
        {toast.message}
      </div>
    </Modal>
  {/if}
{/each}
