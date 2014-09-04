[#ftl]
[@b.navbar brand="Hibernate Configuration&Statistic Browser"]
  [@b.navlist]
    [@b.navitem href="/hibernate/config/${Parameters['session_factory_id']}/index"]Config[/@]
    [@b.navitem href="/hibernate/config/${Parameters['session_factory_id']}/hbm"]Hbm[/@]
    [@b.navitem href="/hibernate/stat/${Parameters['session_factory_id']}/index"]Statistics[/@]
  [/@]
  <p class="navbar-text navbar-right">${Parameters['session_factory_id']}</p>
[/@]
