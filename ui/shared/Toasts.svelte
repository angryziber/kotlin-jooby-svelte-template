<script>
  import {fly} from 'svelte/transition'
  import {hideToast, toastStore} from './toastStore'
  import Modal from './Modal.svelte'
</script>

<div class="toast-container position-fixed bottom-0 end-0 p-3">
  {#each $toastStore as toast (toast.id)}
    {#if !toast.modal}
      <div aria-atomic="true" aria-live="assertive" role="alert" class="toast show bg-{toast.type}" transition:fly={{y: 100}}>
        <div class="toast-header">
          <strong class="me-auto">{toast.title || toast.message}</strong>
          <button type="button" class="btn-close" aria-label="Close" on:click={() => hideToast(toast)}></button>
        </div>
        {#if toast.title}
          <div class="toast-body text-preserve-lines">
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
      <div class="alert alert-{toast.type} font-weight-normal h5 mb-0 text-preserve-lines">
        {toast.message}
      </div>
    </Modal>
  {/if}
{/each}
