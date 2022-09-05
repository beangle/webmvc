[#ftl]
<li>[#if tag.label??]<label class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]
 <input type="file" name="${tag.name}" [#if tag.id??]id="${tag.id}"[/#if] title="${tag.label!'File'}" onchange="jQuery(this).data('file',event.target.files[0]);beangle.displayFileInfo('${tag.id}_filesize',event.target.files[0],${tag.maxSize}*1024);" ${tag.parameterString}>
 <label class="comment">${tag.comment!}<span id="${tag.id}_filesize">不超过${tag.maxSize?number/1024.0}MB[#if tag.extensions?length>0],格式为${tag.extensions}的文件[/#if]</span></label>
</li>
