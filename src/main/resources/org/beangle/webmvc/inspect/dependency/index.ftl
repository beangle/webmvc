[@b.head/]
<div class="container">
[#include "../nav.ftl"/]
[@b.grid items=jarPoms?sort_by("groupId") var="pom" class="border-1px border-colored" caption="Jars and Web Modules(Requires Maven 2 data)"]
  [@b.row]
    [@b.col title="Index" width="5%"]${pom_index+1}[/@]
    [@b.col title="Group ID" property="groupId"/]
    [@b.col title="Artifact ID" property="artifactId"/]
    [@b.col title="Version" property="version"/]
  [/@]
[/@]
</div>
[@b.foot/]
