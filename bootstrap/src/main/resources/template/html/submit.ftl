[#ftl]
<input type="submit" [#if tag.id??]id="${tag.id}"[/#if] [#if tag.cssClass??]class="${tag.cssClass}"[/#if] value="${tag.value!'Submit'}" onclick="bg.form.submit('${tag.formId}',[#if tag.action??]'${tag.action}'[#else]null[/#if],[#if tag.target??]'${tag.target}'[#else]null[/#if],[#if tag.onsubmit??]${tag.onsubmit}[#else]null[/#if]);return false;"${tag.parameterString}/>
