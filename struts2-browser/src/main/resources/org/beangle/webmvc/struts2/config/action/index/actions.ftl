[#ftl]
[#if Parameters['namespace']??]
<h4>Actions in [#if namespace == ""] default namespace [#else] ${namespace} [/#if]</h4>
  <ul>
  [#list actionNames as name]
  <li>[@b.a href="!config?namespace=${namespace}&actionName=${name}" target="action_content"]&nbsp;${name}&nbsp;[/@]</li>
  [/#list]
  </ul>
[#else]

<div class="container">
  <div class="row">
   <div class="col-md-2">
   Namespaces
     <ul class="nav nav-pills nav-stacked">
         [#list namespaces as namespace]
         <li>[@b.a href="!actions?namespace=${namespace}" target="action_content"][#if namespace=""]default[#else]${namespace}[/#if][/@]</li>
         [/#list]
      </ul>
   </div>
   [@b.div id="action_content" class="col-md-10" href="!actions?namespace=${namespaces?first}"/]
  </div>
</div>
[/#if]
