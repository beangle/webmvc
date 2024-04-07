[#ftl]
[@b.head/]
[#include "../nav.ftl"/]
<div class="container-fluid">
  <div class="row">
   <div class="col-md-2">
   Profiles & Namespaces
     [@b.nav class="nav flex-column nav-pills"]
       [@b.navitem active=true href="!profiles" target="action_content"]Profiles[/@]
       [#list namespaces as namespace]
       [@b.navitem href="!actions?namespace=${namespace}" target="action_content"][#if namespace=""]default[#else]${namespace}[/#if][/@]
       [/#list]
       [@b.navitem active=false href="!jekyll" target="action_content"]Jekyll[/@]
      [/@]
   </div>
   [@b.div id="action_content" class="col-md-10" href="!profiles"/]
  </div>
</div>
[@b.foot/]
