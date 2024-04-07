[@b.head/]
<div class="container">
  [#include "../../nav.ftl"/]
  [#if factories?size>0]
  Found SessionFactories.
  [#else]
  Cannot find session factories.
  [/#if]
  <ul>
  [#list factories?keys as k]
  <li>[@b.a href="config/{session_factory_id}?session_factory_id=${k}"]${k}[/@]</li>
  [/#list]
  </ul>
</div>
[@b.foot/]
