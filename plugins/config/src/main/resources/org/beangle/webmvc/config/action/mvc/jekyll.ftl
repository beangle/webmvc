[@b.head/]
[@b.form action="!jekyll"]
  [@b.textfield name="packageName" label="Action Package Name" value="${Parameters['packageName']!}"/]
  [@b.submit/]
[/@]
[#macro gen_jekyll]
---
layout: page
title: {{Title}}
---
{% include JB/setup %}

${base}下共计${actionNames?size}个服务，如下表:

<table class="table table-bordered table-striped table-condensed">
    <tr>
      <th>序号</th>
      <th>名称</th>
      <th>描述</th>
    </tr>
  [#list actionNames as name]
  <tr>
    <td>${name_index+1}</td>
    <td>
      [#if name?length!=0]<a href="#action${name?replace("/","_")}">${name}</a>[/#if]
    </td>
    <td>
      ${descriptions.get(name)!}
    </td>
  </tr>
  [/#list]
</table>

[#list actionNames as name]
[#assign config = configMap[name]]

<h4 id="action${name?replace("/","_")}">${name_index+1} ${base}${name} ${descriptions.get(name)!}</h4>

<table class="table table-bordered table-striped table-condensed">
   <tr>
    <th>序号</th>
    <th>名称</th>
    <th>HTTP方法</th>
    <th>参数(*为必须)</th>
    <th>描述</th>
   </tr>
 [#list config.mappings?keys as mappingkey]
 [#assign mapping = config.mappings.get(mappingkey)/]
 <tr>
 <td>${mappingkey_index+1}</td>
 <td>[#if mapping.name?length>0]${base}${config.name}/${mapping.name}[#else]${base}${config.name}[/#if]</td>
 <td>${mapping.httpMethod!"*"}</td>
 <td>[#list mapping.arguments as p]${p.toString()}[#if p_has_next],[/#if][/#list]</td>
 <td>[#if mapping.method.name=="index"]${messages.get(config.clazz,"class")!}[#else]${messages.get(config.clazz,mapping.method.name)!}[/#if]</td>
 </tr>
 [/#list]
</table>

[/#list]
[/#macro]
<pre>
[#assign jekyll_text][@gen_jekyll/][/#assign]
${jekyll_text?html}
</pre>
[@b.foot/]