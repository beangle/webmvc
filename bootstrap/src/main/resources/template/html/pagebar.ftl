<script type="text/javascript">
bg.ready(function(){
  page_${tag.id}=bg.page("${request.requestURI}","${tag.parameters['target']!""}");
  page_${tag.id}.totalPages=${tag.page.totalPages};
  page_${tag.id}.addParams('${b.paramstring}');
  page_${tag.id}.target(null,'${tag.id}');
});
</script>
[#if tag.parameters['fixPageSize']?? && (tag.parameters['fixPageSize']=='1' || tag.parameters['fixPageSize']=='true')][#assign fixPageSize=true][#else][#assign fixPageSize=false][/#if]
<div [#if tag.parameters['cssClass']??]class="${tag.parameters['cssClass']}"[#else]class="girdbar-pgbar"[/#if] [#if tag.parameters['cssStyle']??]style="${tag.parameters['style']}"[/#if]>
  [#if tag.page.pageIndex==1]${b.text("page.first")} [#else]
  <a href="#" onclick="page_${tag.id}.goPage(1)">${b.text("page.first")}</a>
  <a href="#" onclick="page_${tag.id}.goPage(${tag.page.pageIndex-1})" >${b.text("page.previous")}</a>
  [/#if]
  <input type="text" name="pageIndex" value="${tag.page.pageIndex}" title="当前页"
  onchange="page_${tag.id}.goPage(this.value)" style="width:30px;background-color:#CDD6ED"/>
  [#if !(tag.page.hasNext())]共${tag.page.totalPages}页[#else]
  <a href="#" onclick="page_${tag.id}.goPage(${tag.page.pageIndex+1})" >${b.text("page.next")}</a>
  <a href="#" onclick="page_${tag.id}.goPage(${tag.page.totalPages})" title="${b.text("page.last")}">共${tag.page.totalPages}页</a>
  [/#if]
  [#if !fixPageSize!false]
  每页[#assign pageRank=[10,15,20,25,30,50,70,90,100,150,300,1000]]
  <select name="pageSize" onchange="page_${tag.id}.goPage(1,this.value)" title="page size">
  [#list pageRank as rank]<option value="${rank}" [#if tag.page.pageSize=rank]selected="selected"[/#if]>${rank}</option>[/#list]
  </select>
  |${tag.page?size}
  [#else]
  每页${tag.page.pageSize}
  [/#if]
  总${tag.page.totalItems}条
</div>
