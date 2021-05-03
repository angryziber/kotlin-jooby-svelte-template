<script>
  import {_} from '@ui/i18n'
  import Icon from '../shared/Icon.svelte'
  import AppLogo from './AppLogo.svelte'
  import PageMenu from './PageMenu.svelte'
  import LanguageSelection from './LanguageSelection.svelte'
</script>

<div class="viewport">
  <nav class="navbar navbar-expand-lg navbar-light bg-light d-print-none">
    <div class="container-fluid">
      <AppLogo/>
      <PageMenu/>
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        <li class="nav-item">
          <LanguageSelection/>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/logout">
            <span>{$_('logout.linkTitle')}</span>
            <Icon iconClass="ml-2" name="log-out"/>
          </a>
        </li>
      </ul>
    </div>
  </nav>

  <div class="viewport-main-content">
    <slot/>
  </div>
</div>
