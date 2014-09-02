[@b.head/]
[#include "../nav.ftl"/]
<h4> Modules Locations</h4>
<ul>
  [#list container.moduleLocations as ml]
    <li>${ml.URL}</li>
  [/#list]
</ul>
<h4> Modules</h4>
<ul>
  [#list container.modules?sort as m]
    <li>${m.class.name}</li>
  [/#list]
</ul>
<h4> Reconfig Locations</h4>
<ul>
    <li>${container.reconfigLocations!}</li>
</ul>
<h4>
   [#if container.parent??]
   Beans in Web Container([@b.a  href="!index?parent=true"]ROOT[/@])
   [#else]
   Beans in ROOT Container
   [/#if]
</h4>
[@b.grid items=beanNames?sort var="beanName"  style="table-layout:fixed"]
  [@b.row]
    [@b.col title="Index" width="5%"][#assign className=(container.getType(beanName).name)!""/] ${beanName_index+1}[/@]
    [@b.col title="Bean Name"  width="23%" style="word-wrap:break-word;" ][#if className == beanName] --[#else]${beanName}[/#if][/@]
    [@b.col title="Type" width="42%"  ] ${className}[/@]
    [@b.col title="Description"  width="30%"  ]${container.getDefinition(beanName).description!}[/@]
  [/@]
[/@]
<label class='laben-info'> -- 表示和Type相同</label>
[@b.foot/]