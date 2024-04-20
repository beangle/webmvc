[#ftl]
<li[#if tag.id??] id="${tag.id}"[/#if]${tag.parameterString}>[#if tag.label??]<label class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]${tag.body}</li>
