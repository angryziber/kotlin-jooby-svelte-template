<script>
  import Icon from '../shared/Icon.svelte'
  import AppLogo from './AppLogo.svelte'
  import PageMenu from './PageMenu.svelte'
  import LanguageSelection from './LanguageSelection.svelte'
</script>

<div class="viewport">
  <div class="sticky-wrapper d-print-none">
    <nav id="main-nav" class="sidebar sidebar-left">
      <a class="navbar-toggle" data-target="#topnav-menu-content" data-toggle="collapse">
        <div class="lines"><span></span><span></span><span></span></div>
      </a>
      <AppLogo/>
      <PageMenu/>
      <ul class="nav navbar-nav navbar-user">
        <li class="nav-item d-none d-md-block">
          <LanguageSelection/>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/logout">
            <span>{$_('logout.linkTitle')}</span>
            <Icon iconClass="ml-2" name="log-out"/>
          </a>
        </li>
      </ul>
    </nav>
  </div>

  <div class="viewport-main-content">
    <slot/>
  </div>
</div>
