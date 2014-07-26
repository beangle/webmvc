[#ftl/]
<nav [#if tag.id??] id="${tag.id}"[/#if] class="navbar navbar-default[#if tag.cssClass??] ${tag.cssClass}[/#if]" role="navigation">
   [#if tag.title??]
   <div class="navbar-header">
      <a class="navbar-brand" href="#">${tag.title}</a>
   </div>
   [/#if]
   <div>
     <ul class="nav navbar-nav">
       ${tag.body!}
      </ul>
   </div>
</nav>
