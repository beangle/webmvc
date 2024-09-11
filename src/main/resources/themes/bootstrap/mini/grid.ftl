<table class="table [#if tag.cssClass??] ${tag.cssClass}[/#if]" id="${tag.id}" ${tag.parameterString}>
[#if tag.cols?size>0]
<thead>
  <tr>
  [#list tag.cols as cln]
    <th [#if cln.width??] width="${cln.width}"[/#if]>${cln.title}</th>
  [/#list]
  </tr>
</thead>
[/#if]
<tbody>${tag.body}</tbody>
</table>
