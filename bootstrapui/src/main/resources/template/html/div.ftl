[#ftl]
<div [#if tag.id??]id="${tag.id}"[/#if]${tag.parameterString}>${tag.body}</div>
[#if tag.href??]<script>bg.ready(function(){bg.Go('${tag.href}','${tag.id}')});</script>[/#if]