[#ftl/]
<script type="text/javascript">beangle.ui.load("My97DatePicker");</script>
<tr><td class="search-item">[#if tag.label??]<label for="${tag.id}">${tag.label}:</label>[/#if]<input type="text" id="${tag.id}" [#if tag.title??]title="${tag.title}"[/#if] class="Wdate" onFocus="WdatePicker({dateFmt:'${tag.format}'[#if tag.maxDate??],maxDate:'${tag.maxDate}'[/#if][#if tag.minDate??],minDate:'${tag.minDate}'[/#if]})" name="${tag.name}" value="${tag.value}" ${tag.parameterString}/></td></tr>
