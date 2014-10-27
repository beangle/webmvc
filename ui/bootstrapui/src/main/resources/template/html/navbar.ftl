<nav [#if tag.id??] id="${tag.id}"[/#if] class="navbar navbar-default[#if tag.cssClass??] ${tag.cssClass}[/#if]" role="navigation">
   [#if tag.brand??]
   <div class="navbar-header">
      <a class="navbar-brand" href="#">${tag.brand}</a>
   </div>
   [/#if]
   <div>${tag.body!}</div>
</nav>
