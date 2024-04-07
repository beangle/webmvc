[#ftl]
<form id="${tag.id}" name="${tag.name}" [#if tag.cssClass??] class="${tag.cssClass}"[/#if] action="${tag.action}" method="${tag.method}" [#if tag.target??]target="${tag.target}"[/#if] [#if tag.enctype??]enctype="${tag.enctype}"[/#if] ${tag.parameterString} [#if tag.validate=="true" || tag.onsubmit??]onsubmit="return onsubmit${tag.id}()"[/#if]>
[#if Parameters['_params']??]<input name="_params" type="hidden" value="${Parameters['_params']?html}" />[/#if]
${tag.body}
</form>
[#if (tag.validate!"")=="true" ||tag.onsubmit??]
<script>
beangle.load(["jquery-validity"]);
function onsubmit${tag.id}(){
  var res=null;
[#if (tag.validate!"")=="true"]
  jQuery.validity.start();
  ${tag.validity}
  res = jQuery.validity.end().valid;
[/#if]
  if(false==res) return false;
  [#if tag.onsubmit??]
  var nativeOnsubmit${tag.id} = function(){${tag.onsubmit}}
  try{res=nativeOnsubmit${tag.id}();}catch(e){alert(e);return false;}
  [/#if]
  return res;
}
</script>
[/#if]
