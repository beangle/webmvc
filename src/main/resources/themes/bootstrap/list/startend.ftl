[#ftl/]
<script type="text/javascript">beangle.load(["my97"]);</script>
<li>
[#if tag.label??]<label for="${tag.dates?first.id}" class="title">[#if (tag.required!"")?contains("true")]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]
[#list tag.dates as date]
[#if date_index=1]~[/#if]
<input type="text" id="${date.id}" [#if date.title??]title="${date.title}"[/#if]
class="Wdate" onFocus="WdatePicker({dateFmt:'${tag.format}'[#if date.maxDate??],maxDate:'${date.maxDate}'[/#if][#if date.minDate??],minDate:'${date.minDate}'[/#if]})" name="${date.name}" value="${(date.value)?if_exists}" ${tag.parameterString}/>
[#if (date.required!"")=="true"]<em class="required"><sup>*</sup></em>[/#if]
[/#list]
[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
</li>
