[#if tag.option??][#assign optionTemplate=tag.option?interpret][/#if]
[#assign remoteSearch=tag.remoteSearch/]
[#assign localChosen=false]
[#if tag.items?? && (tag.items?size > (tag.chosenMin?number-1))]
  [#assign localChosen=true]
[/#if]
[#if tag.label??]<label for="${tag.id}" class="title">[#if (tag.required!"")=="true"]<em class="required">*</em>[/#if]${tag.label}:</label>[/#if]
[#assign selected=false/]
<select id="${tag.id}" [#if tag.title??]title="${tag.title}"[/#if] name="${tag.name}" [#if tag.width??]width="${tag.width}"[/#if] [#if tag.multiple??]multiple="${tag.multiple}"[/#if]${tag.parameterString}>
${tag.body}
[#if tag.empty??][#if localChosen|| remoteSearch]<option value=""></option>[#else]<option value="">${tag.empty}</option>[/#if][/#if][#rt/]
[#if tag.items??]
[#list tag.items as item][#assign optionText][#if tag.option??][@optionTemplate/][#else]${item[tag.valueName]!}[/#if][/#assign]
<option value="${item[tag.keyName]}" title="${optionText!}" [#if (!selected || tag.multiple??) && tag.isSelected(item)] selected="selected" [#assign selected=true/][/#if]>${optionText!}</option>
[/#list]
[#if tag.keys?size>0 && !selected][#list tag.keys as k]<option value="${k}" selected="selected">${k}</option>[/#list][/#if]
[/#if]
[#if remoteSearch]
    [#list tag.values as v]
      [#if v?is_hash]
        <option value="${v[tag.keyName]!}" selected="selected">${v[tag.valueName]!}</option>
      [#else]
        <option value="${v}" selected="selected">${v}</option>
      [/#if]
    [/#list]
[/#if]
</select>[#if tag.comment??]<label class="comment">${tag.comment}</label>[/#if]
[#if localChosen || tag.href??]
<script type="text/javascript">
[#if localChosen]
  beangle.load(["chosen"],function(){
    $("#${tag.id}").chosen({placeholder_text_single:"${tag.empty!'...'}",no_results_text: "没有找到结果！",search_contains:true,allow_single_deselect:true[#if tag.width??],width:'${tag.width}'[/#if]});
  });
[#elseif tag.href??]
  [#if remoteSearch]
  beangle.load(["chosen","bui-ajaxchosen"],function(){
    $("#${tag.id}").ajaxchosen(
    { method:"GET",
      url:"${tag.href}"
    },
    function (datas){
      var items=[]
      jQuery.each(datas,function(i,data){
        items.push({"value":data['${tag.keyName}'],"text":data['${tag.valueName}']});
       });
       return items;
    },{placeholder_text_single:"${tag.empty!'...'}",search_contains:true,allow_single_deselect:true[#if tag.width??],width:'${tag.width}'[/#if]}
    );
  });
  [#else]
  jQuery.ajax({
    url: "${tag.href}",
    headers:{"Accept":"application/json"},
    success: function(datas){
      var select = $("#${tag.id}")
      var cnt=0;
      for(var i in datas){
        cnt += 1;
        var data = datas[i], value = data.${tag.keyName}
        select.append('<option value="'+value+'" title="'+data.${tag.valueName}+'">'+data.${tag.valueName}+'</option>');
      }
      [#if tag.value??]
      select.val("${tag.value}")
      [/#if]
      if( cnt >= ${tag.chosenMin}){
        beangle.load(["chosen"],function(){
          $("#${tag.id}").chosen({placeholder_text_single:"${tag.empty!'...'}",no_results_text: "没有找到结果！",search_contains:true,allow_single_deselect:true[#if tag.width??],width:'${tag.width}'[/#if]});
        });
      }
    }
  });
  [/#if]
[/#if]
</script>
[/#if]
