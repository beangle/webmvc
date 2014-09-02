[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
<div class="container">
  <div class="row">
   <div class="col-md-2">
   Profiles
   <ul class="nav nav-pills nav-stacked">
      <li>[@b.a href="!profiles" target="action_content"]Profiles[/@]</li>
   </ul>
   Namespaces
     <ul class="nav nav-pills nav-stacked">
       [#list namespaces as namespace]
       <li>[@b.a href="!actions?namespace=${namespace}" target="action_content"][#if namespace=""]default[#else]${namespace}[/#if][/@]</li>
       [/#list]
      </ul>
   </div>
   [@b.div id="action_content" class="col-md-10" href="!profiles"/]
  </div>
</div>
[@b.foot/]