[#ftl/]
[@b.head/]
[#include "nav.ftl"/]
<div>
   [#if container.parent??]
   Beans in Web Container([@b.a  href="!beans?parent=true"]ROOT[/@])
   [#else]
   Beans in ROOT Container
   [/#if]
</div>
[@b.grid items=beanNames?sort var="beanName"  style="table-layout:fixed"]
  [@b.row]
    [@b.col title="Index" width="5%"][#assign className=(container.getType(beanName).name)!""/] ${beanName_index+1}[/@]
    [@b.col title="Bean Name"  width="23%" style="word-wrap:break-word;" ][#if className == beanName] --[#else]${beanName}[/#if][/@]
    [@b.col title="Type" width="42%"  ] ${className}[/@]
    [@b.col title="Description"  width="30%"  ]${container.getDefinition(beanName).description!}[/@]
  [/@]
[/@]
<label class='laben-info'>BeanName中的 -- 表示和Type相同</label>
[@b.foot/]