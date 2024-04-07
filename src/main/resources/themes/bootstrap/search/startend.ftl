[#ftl/]
<script type="text/javascript">beangle.load(["my97"]);</script>
[#list tag.dates as date]
<div class="search-item">
<label style="font-weight:inherit" for="${date.id}">${date.label}:</label>
<input type="text" id="${date.id}" [#if date.title??]title="${date.title}"[/#if] class="Wdate" onFocus="WdatePicker({dateFmt:'${tag.format}'[#if date.maxDate??],maxDate:'${date.maxDate}'[/#if][#if date.minDate??],minDate:'${date.minDate}'[/#if]})" name="${date.name}" value="${(date.value)?if_exists}" ${tag.parameterString}/>
</td>
</div>
[/#list]
