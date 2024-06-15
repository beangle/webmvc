[#ftl]
<button type="submit" [#if tag.id??]id="${tag.id}" [/#if]class="${tag.cssClass!'btn btn-outline-primary btn-sm'}" onclick="if(bg.form.submit('${tag.formId}',[#if tag.action??]'${tag.action}'[#else]null[/#if],[#if tag.target??]'${tag.target}'[#else]null[/#if],[#if tag.onsubmit??]${tag.onsubmit}[#else]null[/#if])){beangle.form.displayWaiting('${tag.formId}',this)};return false;"${tag.parameterString}>
[#if tag.body?? && tag.body?length>0]${tag.body}[#else]<i class="fa fa-arrow-circle-right fa-sm"></i> ${tag.value!'Submit'}[/#if]
</button>
