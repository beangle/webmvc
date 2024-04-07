[#if tag.href??]
<div id="${tag.id}" class="tab-pane ajax_container"></div>
[#else]
<div id="${tag.id}" class="tab-pane">${tag.body}</div>
[/#if]
