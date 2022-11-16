[#assign existRemote=false/]
<ul id="${tag.id}" class="nav nav-tabs nav-tabs-compact" ${tag.parameterString}>
  [#list tag.tabs as tab]
  <li class="nav-item"><a href="#${tab.id}" class="nav-link" [#if tab.href??]beangle_href="${tab.href}"[#assign existRemote=true/][/#if] data-toggle="tab">${tab.label}</a></li>
  [/#list]
</ul>
<div id="${tag.id}_content" class="tab-content">
${tag.body}
</div>
<script>
beangle.require(["bootstrap"],function(){
  $(function () {
    [#if existRemote]
    var loadedTabIds={}
    [#list tag.tabs as tab]
      [#if tab.href??]
        [#if tab_index?string==tag.selected]
        loadedTabIds['${tab.id}']=true;
        bg.Go('${tab.href}','${tab.id}');
        [/#if]
      [#else]
        loadedTabIds['${tab.id}']=true;
      [/#if]
    [/#list]
    [/#if]
    $('#${tag.id} li:eq(${tag.selected}) a').tab('show')
    $('#${tag.id} a').click(function (e) {
      [#if existRemote]
      var href=e.target.href;
      var tabid=href.substr(href.lastIndexOf('#')+1);
      if(!loadedTabIds[tabid]){
        bg.Go(e.target.getAttribute('beangle_href'),tabid);
        loadedTabIds[tabid]=true;
      }
      [/#if]
      e.preventDefault();
      $(this).tab('show');
    });
  });
});
</script>
