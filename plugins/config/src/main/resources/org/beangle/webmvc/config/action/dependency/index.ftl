[@b.head/]
[#include "../nav.ftl"/]
<div class="container-fluid">
[@b.grid items=jarPoms?sort_by("groupId") var="pom" caption="Jars and Web Modules(Requires Maven 2 data)"]
  [@b.row]
    [@b.col title="Index" width="5%"]${pom_index+1}[/@]
    [@b.col title="Group ID" property="groupId"/]
    [@b.col title="Artifact ID" property="artifactId"/]
    [@b.col title="Version" property="version"/]
  [/@]
[/@]
</div>
[@b.foot/]