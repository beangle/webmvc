[#ftl]
<fieldset[#if !tag.title??] class="emptytitle"[/#if]>
[#if tag.title??]<legend>${tag.title}</legend>[/#if]
${tag.body}
</fieldset>
