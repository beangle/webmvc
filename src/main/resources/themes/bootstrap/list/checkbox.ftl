[#ftl]
<li>[#if tag.label??]<label class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]
<input type="checkbox" id="${tag.id}" name="${tag.name}" value="${tag.value}"${tag.parameterString} [#if tag.checked]checked="checked"[/#if]/>
<label for="${tag.id}" style="font-weight:inherit">${tag.title!}</label>
<span id="${tag.id}_span" style="display:none"></span>
[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
</li>
