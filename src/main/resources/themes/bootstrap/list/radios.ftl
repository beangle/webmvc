[#ftl]
<li>[#if tag.label??]<label class="title"><em class="required">*</em>${tag.label}:</label>[/#if]
<div class="btn-group btn-group-toggle" data-toggle="buttons" style="height: 1.5625rem;">
[#list tag.radios as radio]
    <label style="font-size:0.8125rem !important;padding:2px 8px 0px 8px;" class="btn btn-outline-secondary btn-sm [#if (tag.value!"")== radio.value]active[/#if]">
    <input type="radio" name="${tag.name}" id="${radio.id}" ${tag.parameterString} value="${radio.value}" [#if (tag.value!"")== radio.value]checked[/#if]> ${radio.title!}
  </label>
[/#list]
</div>
<span id="${tag.id}" style="display:none"></span>
[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
</li>
