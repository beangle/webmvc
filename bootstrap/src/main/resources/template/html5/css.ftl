[#ftl/]
<link [#if tag.id??]id="${tag.id}"[/#if] href="${b.uitheme.cssurl((Parameters['ui.theme']!"default"),tag.href)}" rel="stylesheet" type="text/css" />