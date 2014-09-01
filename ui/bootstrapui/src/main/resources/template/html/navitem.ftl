[#ftl]
<li [#if tag.selected]class="active"[/#if]>
[#if tag.href??]
<a href="${tag.href}"  [#if tag.onclick??]onclick="${tag.onclick}"[/#if] ${tag.parameterString}>${tag.body!}</a>
[#else]
  ${tag.body!}
[/#if]
</li>