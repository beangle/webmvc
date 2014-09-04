[#ftl]
[@b.head/]
<h4>Actions in [#if namespace == ""] default namespace [#else] ${namespace} [/#if]</h4>
  <ul>
  [#list actionNames as name]
  <li>[@b.a href="!action?name=${namespace}/${name}" target="action_content"]&nbsp;${name}&nbsp;[/@]</li>
  [/#list]
  </ul>
[@b.foot/]