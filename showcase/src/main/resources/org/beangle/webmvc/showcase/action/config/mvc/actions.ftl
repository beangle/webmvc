[#ftl]
[@b.head/]
<h4>Actions in [#if namespace == ""] default namespace [#else] ${namespace} [/#if]</h4>
  <ul>
  [#list actionNames as name]
  [#if name?length==0]
  <li>[@b.a href="!action?name=${namespace}" target="action_content"]&nbsp;空&nbsp;[/@]</li>
  [#else]
  <li>[@b.a href="!action?name=${namespace}/${name}" target="action_content"]&nbsp;${name}&nbsp;[/@]</li>
  [/#if]
  [/#list]
  </ul>
[@b.foot/]
