[#ftl]
<form id="${tag.id}" name="${tag.name}" [#if tag.cssClass??] class="${tag.cssClass}"[/#if] action="${tag.action}" method="post"[#if tag.target??] target="${tag.target}"[/#if][#if tag.onsubmit??] onsubmit="${tag.onsubmit}"[/#if]>
[#if Parameters['_params']??]<input name="_params" type="hidden" value="${Parameters['_params']?html}" />[/#if]
<div class="search-widget">
[#if tag.title??]
<div class="search-header">
  <span class="toolbar-icon action-info"></span><em>${tag.title}</em>
</div>
[@b.hairline/]
[/#if]
${tag.body}
[#if !tag.body?contains('submit')]
<div class="search-footer">[@b.submit value="${b.text('action.search')}"/]</div>
[/#if]
</div>
</form>
