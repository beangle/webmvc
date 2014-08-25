[#ftl/]
[@b.head/]
[#include "nav.ftl"/]
[@b.grid items=jarPoms?sort_by("groupId") var="pom" caption="Jars and Web Modules(Requires Maven 2 data)"]
  [@b.row]
    [@b.col title="Index" width="5%"]${pom_index+1}[/@]
    [@b.col title="Group ID" property="groupId"/]
    [@b.col title="Artifact ID" property="artifactId"/]
    [@b.col title="Version" property="version"/]
  [/@]
[/@]
<br />
<h4> Discovered Web Modules</h4>
<ul>
  [#list pluginsLoaded as url]
    <li>${url}</li>
  [/#list]
</ul>
[@b.foot/]