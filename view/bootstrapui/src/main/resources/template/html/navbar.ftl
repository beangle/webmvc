<nav [#if tag.id??] id="${tag.id}"[/#if] class="navbar navbar-default[#if tag.cssClass??] ${tag.cssClass}[/#if]" role="navigation" ${tag.parameterString}>
   [#if tag.brand??]
   <div class="navbar-header">
      [#if tag.brand?contains("<")]
      ${tag.brand}
      [#else]
      <a class="navbar-brand" href="#">${tag.brand}</a>
      [/#if]
   </div>
   [/#if]
   <div>${tag.body!}</div>
</nav>
