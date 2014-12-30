[@b.head/]
[#include "../nav.ftl"/]
<div class="container-fluid">
<h4> Modules Locations[#if container.parent??](parent is [@b.a  href="!index?parent=true"]ROOT[/@])[/#if]</h4>
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

</h4>
[#assign caption]
   [#if container.parent??]Beans in Web Container[#else]Beans in ROOT Container[/#if]
[/#assign]
[@b.grid items=beanNames?sort var="beanName" caption=caption style="table-layout:fixed"]
  [@b.row]
    [@b.col title="Index" width="5%"]
      [#assign className=(container.getType(beanName).name)!""/]
      [#assign defn =container.getDefinition(beanName)/]
      [#if className==""]
        [#if (defn.beanClass.name)??][#assign className=defn.beanClass.name/]
        [#else] [#assign className=defn.beanClass/] [/#if]
      [/#if]
       ${beanName_index+1}
    [/@]
    [@b.col title="Bean Name"  width="23%" style="word-wrap:break-word;" ][#if className == beanName] --[#else]${beanName}[#if defn.primary]<span style="color:red" title="primitive"> √</span>[/#if][/#if][/@]
    [@b.col title="Type" width="42%"  ] ${className}[/@]
    [@b.col title="Description"  width="30%"]${defn.description!}[#if defn.abstract](抽象模版)[#else][/#if][/@]
  [/@]
[/@]
<label class='laben-info'> -- 表示和Type相同</label>
</div>
[@b.foot/]