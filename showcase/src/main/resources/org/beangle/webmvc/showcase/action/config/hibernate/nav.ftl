[#ftl]
[@b.navbar brand="Hibernate Configuration&Statistic Browser"]
  [@b.navlist]
    [@b.navitem href="config/{session_factory_id}!index"]Config[/@]
    [@b.navitem href="stat/{session_factory_id}!index"]Statistics[/@]
  [/@]
  <p class="navbar-text navbar-right">${Parameters['session_factory_id']}</p>
[/@]
